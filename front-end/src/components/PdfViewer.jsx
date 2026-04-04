// src/components/PdfViewer.jsx
import { useState } from "react";
import { Document, Page, pdfjs } from "react-pdf";
import "../styles/TextLayer.css";
import "../styles/AnnotationLayer.css";

pdfjs.GlobalWorkerOptions.workerSrc = "/pdf.worker.min.js";

function PdfViewer({ file, onPageChange }) {
  const [numPages, setNumPages] = useState(null);
  const [pageNumber, setPageNumber] = useState(1);
  const [scale, setScale] = useState(1.2); // Começa com um zoom de 120%

  function onDocumentLoadSuccess({ numPages }) {
    setNumPages(numPages);
  }

  function handlePageChange(newPage) {
    setPageNumber(newPage);
    if (onPageChange) onPageChange(newPage);
  }

  // Funções para aumentar e diminuir o zoom do PDF
  const zoomIn = () => setScale((prev) => Math.min(prev + 0.2, 3.0)); // Máximo 300%
  const zoomOut = () => setScale((prev) => Math.max(prev - 0.2, 0.5)); // Mínimo 50%

  return (
<div className="flex flex-col items-center w-full h-full gap-4">

{/* Barra de Ferramentas (Paginação e Zoom) */}
<div className="flex flex-wrap items-center justify-between w-full bg-[#1a2c4e] p-3 rounded-t-lg border-b border-white/5 gap-20">
  
  {/* Controles de Zoom */}
    <div className="flex items-center gap-4 bg-[#0b1220] rounded-lg p-2 border border-white/10">
      
      {/* Botão Menos */}
      <button 
        onClick={zoomOut} 
        className="w-10 h-10 flex items-center justify-center hover:bg-[#253f6e] rounded-lg text-white text-3xl font-bold transition-colors"
      >
        -
      </button>

      <span className="text-gray-300 text-lg font-medium w-16 text-center">
        {Math.round(scale * 100)}%
      </span>

      {/* Botão Mais */}
      <button 
        onClick={zoomIn} 
        className="w-10 h-10 flex items-center justify-center hover:bg-[#253f6e] rounded-lg text-white text-3xl font-bold transition-colors"
      >
        +
      </button>
      
    </div>

  {/* Controles de Página */}
  <div className="flex items-center gap-3">
    <button 
      onClick={() => handlePageChange(Math.max(1, pageNumber - 1))} 
      disabled={pageNumber <= 1}
      className="px-3 py-1.5 bg-blue-600 hover:bg-blue-500 text-white text-sm font-medium rounded disabled:bg-gray-700 disabled:text-gray-400 disabled:cursor-not-allowed transition-colors"
    >
      Anterior
    </button>

  {/* Caixa de Input Direto */}
  <div className="flex items-center gap-2 bg-[#0b1220] px-2 py-1 rounded-md border border-white/10">
    <input
        value={pageNumber}
      onChange={(e) => {
        const val = parseInt(e.target.value);
        if (!isNaN(val) && val > 0 && val <= (numPages || 1)) {
          handlePageChange(val);
        } else if (e.target.value === "") {
          // Permite apagar o número para digitar outro
          setPageNumber("");
        }
        if (e.target.value === "") {
          setPageNumber(1);
        }
      }}
      className="w-12 bg-transparent text-center text-white text-sm font-bold focus:outline-none focus:ring-1 focus:ring-blue-500 rounded"
    />
    <span className="text-gray-500 text-xs">/ {numPages || "?"}</span>
  </div>

  <button
    onClick={() => handlePageChange(Math.min(numPages || pageNumber, pageNumber + 1))}
    disabled={!numPages || pageNumber >= numPages}
    className="px-7 py-1.5 bg-blue-600 hover:bg-blue-500 text-white text-sm font-medium rounded disabled:bg-gray-700 disabled:text-gray-400 disabled:cursor-not-allowed transition-colors"
  >
    Próxima
  </button>
  </div>

  </div>

  {/* A "Section" do PDF:
    overflow-auto cria a barra de rolagem APENAS no PDF caso o zoom fique grande.
    h-[75vh] garante que ele tenha uma altura fixa baseada na tela do usuário.
  */}
  <div className="bg-white/5 w-full h-[75vh] overflow-auto flex justify-center p-4 rounded-b-lg border border-white/10 custom-scrollbar">
    <div className="bg-white shadow-2xl rounded-md transition-transform">
      <Document file={file} onLoadSuccess={onDocumentLoadSuccess}>
        {/* Agora usamos a propriedade 'scale' original da biblioteca ao invés de forçar a largura */}
        <Page
          pageNumber={pageNumber}
          scale={scale}
          renderTextLayer={true}
          renderAnnotationLayer={true}
        />
      </Document>
    </div>
  </div>

</div>
  );
}

export default PdfViewer;