import { useState, useEffect, useRef } from "react";
import PdfViewer from "../components/PdfViewer";
import { Link, useLocation } from "react-router-dom";
import { endReadingSession } from "../features/books/readSessions.js";
import { useNavigate } from "react-router-dom";
import { useToast } from "../components/ToastContext.js";
import ReactMarkdown from "react-markdown";
import api from "../features/axiosApi.js";
import "../styles/Leitor.css";

function getRequestErrorMessage(error, fallback) {
  const data = error?.response?.data;

  if (typeof data === "string" && data.trim()) return data;
  if (data?.message) return data.message;
  if (typeof data?.detail === "string") return data.detail;
  if (Array.isArray(data?.detail)) {
    const details = data.detail
      .map((item) => item?.msg || item?.message)
      .filter(Boolean)
      .join(" ");

    if (details) return details;
  }

  if (error?.code === "ERR_NETWORK") return "Não foi possível conectar ao servidor.";

  return error?.message || fallback;
}

export default function Leitor() {
  const { showToast, showAchievementToast } = useToast();
  const navigate = useNavigate();
  const { state } = useLocation();
  const book = state?.book;
  const sessionId = state?.sessionId;
  const initialPage = state?.initialPage || 1;

  const [question, setQuestion] = useState("");
  const [messages, setMessages] = useState([]);
  const [loading, setLoading] = useState(false);
  const [currentPage, setCurrentPage] = useState(initialPage);
  const [endingSession, setEndingSession] = useState(false);
  const [pdfFile, setPdfFile] = useState(null);
  const [pdfError, setPdfError] = useState("");
  const [drawerOpen, setDrawerOpen] = useState(true);

  const chatEndRef = useRef(null);
  const textareaRef = useRef(null);

  const SUGGESTIONS = ["Quem é o personagem principal?", "Resumir este capítulo", "Temas desta passagem"];
  const bookTitle = book?.title || book?.name || "Livro";
  const aiReady = Boolean(book?.id && book?.dataPath);
  const aiStatus = loading ? "Analisando" : aiReady ? "Pronto" : "Sem PDF";

  const handleEndSession = async () => {
    if (!sessionId) {
      showToast("Sessão não encontrada.", "error");
      return;
    }
    setEndingSession(true);
    try {
      const response = await endReadingSession(sessionId, currentPage);
      const novas = response?.data?.novasConquistas || response?.novasConquistas;
      if (novas && novas.length > 0) {
        novas.forEach(conquista => showAchievementToast(conquista));
      }
      showToast("Sessão de leitura encerrada!", "success");
      navigate("/meus-livros");
    } catch (err) {
      showToast("Erro ao encerrar sessão: " + err.message, "error");
    } finally {
      setEndingSession(false);
    }
  };

  useEffect(() => {
    const loadPDF = async () => {
      setPdfError("");
      setPdfFile(null);
      try {
        const response = await api.get(`/${book.dataPath}`, { responseType: "blob" });
        setPdfFile(response.data);
      } catch (error) {
        console.error("Erro ao carregar PDF:", error);
        setPdfError(getRequestErrorMessage(error, "Não foi possível carregar este PDF."));
      }
    };
    if (book?.dataPath) {
      loadPDF();
    } else if (book) {
      setPdfFile(null);
      setPdfError("Este livro não tem PDF cadastrado.");
    }
  }, [book]);

  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  // ---------------------------------------------------------------------------
  // AI — sends question + current page to Java, which forwards to Python/Ollama
  // ---------------------------------------------------------------------------
  const handleSubmit = async (e) => {
    e?.preventDefault();
    if (!question.trim() || loading) return;

    const userMsg = question.trim();
    setQuestion("");
    setMessages((prev) => [...prev, { role: "user", content: userMsg }]);

    if (!aiReady) {
      setMessages((prev) => [
        ...prev,
        { role: "ai", content: "Este livro ainda não tem um PDF disponível para análise." },
      ]);
      return;
    }

    setLoading(true);

    try {
      const response = await api.post("/ai/ask", {
        bookId: book.id,
        page: currentPage,
        question: userMsg,
      });
      setMessages((prev) => [...prev, { role: "ai", content: response.data.answer }]);
    } catch (error) {
      const message = getRequestErrorMessage(error, "Não foi possível obter resposta da IA.");
      setMessages((prev) => [
        ...prev,
        { role: "ai", content: `Erro ao obter resposta: ${message}` },
      ]);
    } finally {
      setLoading(false);
    }
  };

  // ---------------------------------------------------------------------------
  // Search — delegates to Python via Java, returns [{ page, excerpt }]
  // ---------------------------------------------------------------------------
  const handleSearchRequest = async (query) => {
    try {
      const response = await api.post("/ai/search", {
        bookId: book.id,
        query,
      });
      return response.data.results || []; // Array<{ page: number, excerpt: string }>
    } catch (error) {
      throw new Error(getRequestErrorMessage(error, "Não foi possível buscar no PDF."));
    }
  };

  // ---------------------------------------------------------------------------
  // Text mode — fetches plain text for a page from Python via Java
  // ---------------------------------------------------------------------------
  const handleTextPageRequest = async (pageNumber) => {
    try {
      const response = await api.get("/ai/page-text", {
        params: { bookId: book.id, page: pageNumber },
      });
      return response.data.text;
    } catch (error) {
      throw new Error(getRequestErrorMessage(error, "Não foi possível obter o texto desta página."));
    }
  };

  const handleKeyDown = (e) => {
    if ((e.metaKey || e.ctrlKey) && e.key === "Enter") handleSubmit();
  };

  const handleChipClick = (text) => {
    setQuestion(text);
    textareaRef.current?.focus();
  };

  if (!book) {
    return (
      <div style={{ padding: "2rem", fontFamily: "sans-serif" }}>
        <Link to="/home">← Voltar</Link>
        <p style={{ marginTop: "1rem" }}>Livro não encontrado.</p>
      </div>
    );
  }

  return (
    <div className="leitor-root">

      {/* ── TOP BAR ── */}
      <header className="leitor-topbar">
        <div className="leitor-topbar-left">
          <button
            className="leitor-back-btn"
            onClick={async (e) => {
              e.preventDefault();
              await handleEndSession();
            }}
            disabled={endingSession}
          >
            <svg width="14" height="14" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
              <path d="M19 12H5M12 5l-7 7 7 7" />
            </svg>
            {endingSession ? "Encerrando…" : "Voltar"}
          </button>

          <div className="leitor-book-meta">
            <span className="leitor-book-title">{bookTitle}</span>
            <span className="leitor-book-subtitle">p. {currentPage}</span>
          </div>
        </div>

        <div className="leitor-topbar-right">
          <div className="leitor-page-nav">
            <span className="leitor-page-label">
              Página <strong>{currentPage}</strong>
            </span>
          </div>

          <button
            className={`leitor-ai-toggle ${drawerOpen ? "active" : ""}`}
            onClick={() => setDrawerOpen((v) => !v)}
          >
            <svg width="14" height="14" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
              <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z" />
            </svg>
            Assistente
          </button>
        </div>
      </header>

      {/* ── BODY ── */}
      <div className="leitor-body">

        {/* PDF PANEL */}
        <div className="leitor-pdf-panel">
          <div className="leitor-pdf-body">
            {pdfFile ? (
              <PdfViewer
                file={pdfFile}
                initialPage={initialPage}
                onPageChange={setCurrentPage}
                storageKey={book.id || book.dataPath || "livro"}
                onSearchRequest={aiReady ? handleSearchRequest : undefined}
                onTextPageRequest={aiReady ? handleTextPageRequest : undefined}
              />
            ) : pdfError ? (
              <div className="leitor-pdf-loading">
                <span className="leitor-loading-text leitor-loading-error">{pdfError}</span>
              </div>
            ) : (
              <div className="leitor-pdf-loading">
                <div className="leitor-spinner" />
                <span className="leitor-loading-text">Carregando PDF…</span>
              </div>
            )}
          </div>
        </div>

        {/* AI DRAWER */}
        <div className={`leitor-drawer ${drawerOpen ? "open" : ""}`}>
          <div className="leitor-drawer-inner">

            {/* Drawer header */}
            <div className="leitor-drawer-header">
              <div className="leitor-drawer-header-row">
                <span className="leitor-drawer-title">Assistente</span>
                <span className={`leitor-status-badge ${aiReady ? "active" : "warning"}`}>{aiStatus}</span>
              </div>
              <p className="leitor-context-info">Página {currentPage} em contexto</p>
            </div>

            {/* Chat messages */}
            <div className="leitor-chat-area">
              {messages.length === 0 ? (
                <div className="leitor-empty-state">
                  <svg
                    className="leitor-empty-icon"
                    width="36"
                    height="36"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="1.2"
                    viewBox="0 0 24 24"
                  >
                    <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z" />
                  </svg>
                  <p className="leitor-empty-text">
                    Faça uma pergunta<br />sobre o que está lendo
                  </p>
                </div>
              ) : (
                <>
                  {messages.map((msg, i) => (
                    <div key={i} className={msg.role === "user" ? "leitor-user-bubble" : "leitor-ai-bubble"}>
                      <span className="leitor-bubble-label">
                        {msg.role === "user" ? "Você" : "IA"}
                      </span>
                      {msg.role === "ai" ? (
                        <div className="leitor-ai-text">
                          <ReactMarkdown>{msg.content}</ReactMarkdown>
                        </div>
                      ) : (
                        <p className="leitor-user-text">{msg.content}</p>
                      )}
                    </div>
                  ))}

                  {loading && (
                    <div className="leitor-ai-bubble">
                      <span className="leitor-bubble-label">IA</span>
                      <div className="leitor-thinking-row">
                        <div className="leitor-spinner-small" />
                        <span className="leitor-thinking-text">Analisando…</span>
                      </div>
                    </div>
                  )}

                  <div ref={chatEndRef} />
                </>
              )}
            </div>

            {/* Suggestion chips — only when no messages yet */}
            {messages.length === 0 && (
              <div className="leitor-chips">
                {SUGGESTIONS.map((s) => (
                  <button key={s} className="leitor-chip" onClick={() => handleChipClick(s)}>
                    {s}
                  </button>
                ))}
              </div>
            )}

            {/* Input */}
            <div className="leitor-input-area">
              <div className="leitor-input-wrap">
                <textarea
                  ref={textareaRef}
                  className="leitor-textarea"
                  value={question}
                  onChange={(e) => setQuestion(e.target.value)}
                  onKeyDown={handleKeyDown}
                  placeholder={aiReady ? "Pergunte sobre a obra…" : "Assistente indisponível para este livro"}
                  disabled={loading || !aiReady}
                  rows={2}
                />
                <div className="leitor-input-footer">
                  <span className="leitor-hint">⌘ + Enter</span>
                  <button
                    className="leitor-send-btn"
                    onClick={handleSubmit}
                    disabled={loading || !question.trim() || !aiReady}
                  >
                    {loading ? (
                      <div className="leitor-spinner-small" />
                    ) : (
                      <svg width="14" height="14" fill="none" stroke="currentColor" strokeWidth="2.5" viewBox="0 0 24 24">
                        <line x1="22" y1="2" x2="11" y2="13" />
                        <polygon points="22 2 15 22 11 13 2 9 22 2" />
                      </svg>
                    )}
                    {loading ? "Processando…" : "Enviar"}
                  </button>
                </div>
              </div>
            </div>

          </div>
        </div>
      </div>

    </div>
  );
}
