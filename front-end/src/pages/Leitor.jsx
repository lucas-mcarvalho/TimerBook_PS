import { useState, useEffect } from "react";
import PdfViewer from "../components/PdfViewer";
import { Link, useLocation } from "react-router-dom";
import { endReadingSession } from "../features/books/readSessions.js";
import { extractPDFRange } from "../features/books/pdfExtractor.js";
import { askAI } from "../lib/llama.js";
import { useNavigate } from "react-router-dom";

export default function Leitor() {
  const navigate = useNavigate();
  const { state } = useLocation();
  const book = state?.book;
  const sessionId = state?.sessionId;
  const initialPage = state?.initialPage || 1;
  console.log("Sessão recebida:", sessionId, "Página inicial:", initialPage);
  const [question, setQuestion] = useState("");
  const [answer, setAnswer] = useState("");
  const [loading, setLoading] = useState(false);
  const [pdfContext, setPdfContext] = useState("");
  const [extracting, setExtracting] = useState(false);
  const [currentPage, setCurrentPage] = useState(initialPage);
  const [endingSession, setEndingSession] = useState(false);
  const [pdfFile, setPdfFile] = useState(null);
  const PAGE_RANGE = 2;

  const handleEndSession = async () => {
    if (!sessionId) {
      alert("Sessão não encontrada.");
      return;
    }
    setEndingSession(true);
    try {
      await endReadingSession(sessionId, currentPage);
      alert("Sessão de leitura encerrada!");
      navigate("/");
    } catch (err) {
      alert("Erro ao encerrar sessão: " + err.message);
    } finally {
      setEndingSession(false);
    }
  };

  // Se não houver book, exibe mensagem de erro
  if (!book) {
    return (
      <div>
        <Link to="/">Voltar</Link>
        <p>Livro não encontrado.</p>
      </div>
    );
  }

  // Monta a URL do PDF
  const pdfUrl = book.dataPath?.startsWith("blob:")
    ? book.dataPath
    : `http://localhost:8080/${book.dataPath}`;

  // Carrega o PDF ao montar o componente
  useEffect(() => {
    const loadPDF = async () => {
      try {
        const response = await fetch(pdfUrl);
        const blob = await response.blob();
        setPdfFile(blob);
      } catch (error) {
        console.error("Erro ao carregar PDF:", error);
      }
    };
    if (pdfUrl) {
      loadPDF();
    }
  }, [pdfUrl]);

  // Extrai range de páginas quando a página atual muda
  useEffect(() => {
    if (!pdfFile) return;
    const extractRange = async () => {
      setExtracting(true);
      try {
        const startPage = Math.max(1, currentPage - PAGE_RANGE);
        const endPage = currentPage + PAGE_RANGE;
        const text = await extractPDFRange(pdfFile, startPage, endPage);
        setPdfContext(text);
        console.log(`Páginas ${startPage}-${endPage} carregadas`);
      } catch (error) {
        console.error("Erro ao extrair range:", error);
      } finally {
        setExtracting(false);
      }
    };
    extractRange();
  }, [currentPage, pdfFile]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!question.trim()) return;
    setLoading(true);
    try {
      console.log("Pergunta enviada:", question);
      const response = await askAI(question, pdfContext);
      setAnswer(response);
    } catch (error) {
      setAnswer("Erro ao obter resposta: " + error.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <Link to="/">Ir para Home</Link>
      <h1>Leitor</h1>
      <PdfViewer file={pdfUrl} initialPage={initialPage} onPageChange={setCurrentPage} />

      <div style={{ marginTop: 20, padding: 20, border: "1px solid #ccc" }}>
        {sessionId && (
          <button onClick={handleEndSession} disabled={endingSession} style={{ marginBottom: 16 }}>
            {endingSession ? "Encerrando..." : "Encerrar sessão de leitura"}
          </button>
        )}
        <h2>Assistente de Leitura</h2>
        {extracting && <p>Carregando páginas...</p>}
        {pdfContext && <p style={{ fontSize: 12, color: "#666" }}>✓ Páginas {Math.max(1, currentPage - PAGE_RANGE)}-{currentPage + PAGE_RANGE} carregadas</p>}

        <form onSubmit={handleSubmit}>
          <input
            type="text"
            value={question}
            onChange={(e) => setQuestion(e.target.value)}
            placeholder="Faça uma pergunta sobre o PDF..."
            style={{ width: "100%", padding: 8, marginBottom: 10 }}
            disabled={extracting || !pdfContext}
          />
          <button type="submit" disabled={loading || extracting || !pdfContext}>
            {loading ? "Respondendo..." : extracting ? "Carregando..." : "Perguntar"}
          </button>
        </form>

        {answer && (
          <div
            style={{
              marginTop: 20,
              padding: 10,
              backgroundColor: "#f0f0f0",
              maxHeight: 300,
              overflowY: "auto",
            }}
          >
            <strong>Resposta:</strong>
            <ReactMarkdown>{answer}</ReactMarkdown>
          </div>
        )}
      </div>
    </div>
  );
}