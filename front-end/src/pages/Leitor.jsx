import { useState, useEffect } from "react";
import PdfViewer from "../components/PdfViewer";
import { Link } from "react-router-dom";
import { deleteBook } from "../features/books/booksApi";

export default function Leitor() {
  const [question, setQuestion] = useState("");
  const [answer, setAnswer] = useState("");
  const [loading, setLoading] = useState(false);
  const [pdfContext, setPdfContext] = useState("");
  const [extracting, setExtracting] = useState(false);
  const [currentPage, setCurrentPage] = useState(1);
  const [pdfFile, setPdfFile] = useState(null);
  const PAGE_RANGE = 2; 

  // Carrega o PDF ao montar o componente
  useEffect(() => {
    const loadPDF = async () => {
      try {
        const response = await fetch("/Memorias_do_Subsolo.pdf");
        const blob = await response.blob();
        setPdfFile(blob);
      } catch (error) {
        console.error("Erro ao carregar PDF:", error);
      }
    };

    loadPDF();
  }, []);

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
      <PdfViewer file="/the-road-to-learn-react.pdf" />

      <button onClick={() => deleteBook("bookId")}>Deletar Livro</button>
    </div>
  );
}