import { useState, useEffect } from "react";
import PdfViewer from "../components/PdfViewer";
import { Link, useLocation } from "react-router-dom";
import { endReadingSession } from "../features/books/readSessions.js";
import { extractPDFRange } from "../features/books/pdfExtractor.js";
import { askAI } from "../lib/llama.js";
import { useNavigate } from "react-router-dom";
import ReactMarkdown from "react-markdown";
import api from "../features/axiosApi.js"

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
  const [pdfUrlObject, setPdfUrlObject] = useState(null);

  const handleEndSession = async () => {
    if (!sessionId) {
      alert("Sessão não encontrada.");
      return;
    }
    setEndingSession(true);
    try {
      await endReadingSession(sessionId, currentPage);
      alert("Sessão de leitura encerrada!");
      navigate("/meus-livros");
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
        <Link to="/home">Voltar</Link>
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
      const response = await api.get(`/${book.dataPath}`, {
        responseType: "blob",
      });
      console.log("PDF carregado do backend:", response);
      setPdfFile(response.data);
    } catch (error) {
      console.error("Erro ao carregar PDF:", error);
    }
  };

  if (book?.dataPath) {
    loadPDF();
  }
}, [book]);

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
    <div className="w-full h-screen bg-[#0b1220] text-gray-100 flex overflow-hidden">

      {/* ==========================================
          LADO ESQUERDO (PDF): Ocupa o espaço que sobrar
          ========================================== */}
      {/* flex-1 manda essa div crescer o máximo possível empurrando a IA pra direita */}
      <div className="flex-1 h-full flex flex-col p-4 md:p-8">

        <header className="mb-4">
         <Link
            to="/"
            onClick={async (e) => {
              e.preventDefault(); // impede navegação automática
              await handleEndSession(); // executa sua lógica
            }}
            className="text-blue-400 hover:text-blue-300 mb-2 start-0 inline-block"
          >
            ← Voltar
        </Link>
          <h1 className="text-3xl font-bold">Leitor de PDF</h1>
        </header>

        {/* Caixa escura do Livro */}
        <div className="flex-1 bg-[#14233c] rounded-xl border border-white/10 flex flex-col overflow-hidden">
          <div className="p-3 border-b border-white/10 bg-[#1a2c4e]">
            <h2 className="text-lg font-semibold">Livro</h2>
          </div>
          
          <div className="flex-1 overflow-auto p-4 flex justify-center">
            {pdfFile ? (
                <PdfViewer
                  file={pdfFile}
                  onPageChange={setCurrentPage}
                />
              ) : (
                <p>Carregando PDF...</p>
              )}
          </div>
        </div>

      </div>

     <div className="w-[420px] h-full bg-[#0f1a2d] border-l border-white/5 flex flex-col p-6 flex-shrink-0 shadow-[-10px_0_30px_rgba(0,0,0,0.3)]">
        
        {/* Cabeçalho do Agente */}
        <div className="mb-8 relative">
          <div className="flex items-center gap-3 mb-2">
            <div className={`w-3 h-3 rounded-full ${pdfContext ? 'bg-cyan-400 shadow-[0_0_10px_#22d3ee]' : 'bg-gray-600 animate-pulse'}`}></div>
            <h2 className="text-2xl font-bold bg-gradient-to-r from-white to-gray-400 bg-clip-text text-transparent">
              Assistente
            </h2>
          </div>
          
          <div className="flex items-center gap-2">
            <span className={`text-[10px] uppercase tracking-widest font-black px-2 py-0.5 rounded border ${
              pdfContext ? 'border-cyan-500/30 text-cyan-400 bg-cyan-500/5' : 'border-gray-700 text-gray-500'
            }`}>
              {extracting ? "Analisando..." : pdfContext ? "Live Context" : "Offline"}
            </span>
            <p className="text-xs text-gray-500 font-medium italic">
              {extracting ? "Lendo Dostoievski..." : pdfContext ? "Pronto para debate." : "Aguardando PDF..."}
            </p>
          </div>
        </div>

        {/* Área de Chat (Scrollbox) */}
        <div className="flex-1 bg-[#070c16]/50 border border-white/5 rounded-2xl p-5 mb-6 overflow-y-auto custom-scrollbar shadow-inner relative group">
          {answer ? (
            <div className="space-y-4 animate-in fade-in duration-700">
              <div className="flex items-center gap-2 text-cyan-400">
                <span className="text-[10px] font-bold tracking-tighter uppercase border border-cyan-400/30 px-1 rounded">IA-Core</span>
                <div className="h-[1px] flex-1 bg-gradient-to-r from-cyan-400/20 to-transparent"></div>
              </div>
              
              <div className="prose prose-invert max-w-none text-sm text-gray-300 leading-relaxed font-light selection:bg-cyan-500/30">
                <ReactMarkdown>{answer}</ReactMarkdown>
              </div>
            </div>
          ) : (
            <div className="h-full flex flex-col items-center justify-center opacity-20 group-hover:opacity-30 transition-opacity">
               <svg className="w-12 h-12 mb-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1" d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z" />
               </svg>
               <p className="text-sm font-medium tracking-wide">Inicie o diálogo abaixo</p>
            </div>
          )}
        </div>

        {/* Input e Ações */}
        <form onSubmit={handleSubmit} className="flex flex-col gap-4 bg-[#14233c]/30 p-4 rounded-2xl border border-white/5 backdrop-blur-sm">
          <div className="relative">
            <textarea
              value={question}
              onChange={(e) => setQuestion(e.target.value)}
              placeholder="Pergunte sobre a obra..."
              disabled={extracting || !pdfContext}
              rows="3"
              className="w-full bg-transparent text-sm text-white placeholder-gray-600 focus:outline-none resize-none pr-10"
            />
            <div className="absolute bottom-0 right-0 p-1">
               <span className="text-[10px] text-gray-600 font-mono">⌘+Enter</span>
            </div>
          </div>

          <button
            type="submit"
            disabled={loading || extracting || !pdfContext}
            className="group relative w-full overflow-hidden rounded-xl bg-blue-600 py-3 font-bold text-white transition-all hover:bg-blue-500 active:scale-[0.98] disabled:bg-gray-800 disabled:text-gray-600 disabled:cursor-not-allowed shadow-lg shadow-blue-900/40"
          >
            {/* Efeito de brilho no botão ao passar o mouse */}
            <div className="absolute inset-0 flex h-full w-full justify-center [transform:skew(-12deg)_translateX(-100%)] group-hover:duration-1000 group-hover:[transform:skew(-12deg)_translateX(100%)]">
              <div className="relative h-full w-8 bg-white/20"></div>
            </div>
            
            <span className="relative flex items-center justify-center gap-2">
              {loading ? (
                <>
                  <div className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin"></div>
                  <span>Processando...</span>
                </>
              ) : (
                <>
                  <span>Enviar Pergunta</span>
                  <svg className="w-4 h-4 transition-transform group-hover:translate-x-1" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M13 5l7 7-7 7M5 5l7 7-7 7" /></svg>
                </>
              )}
            </span>
          </button>
        </form>

      </div>

    </div>
  );
}