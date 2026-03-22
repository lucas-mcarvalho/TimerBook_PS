// src/components/PdfViewer.jsx
import { useState } from "react";
import { Document, Page, pdfjs } from "react-pdf";
import "../styles/TextLayer.css";
import "../styles/AnnotationLayer.css";

pdfjs.GlobalWorkerOptions.workerSrc = "/pdf.worker.min.js";
  
function PdfViewer({ file, onPageChange }) {
  const [numPages, setNumPages] = useState(null);
  const [pageNumber, setPageNumber] = useState(1);

  function onDocumentLoadSuccess({ numPages }) {
    setNumPages(numPages);
  }

  function handlePageChange(newPage) {
    setPageNumber(newPage);
    if (onPageChange) {
      onPageChange(newPage);
    }
  }

  return (
    <div style={{ textAlign: "center", width: 700, margin: "auto" }}>
      <h2>Leitor PDF de teste</h2>

      <Document file={file} onLoadSuccess={onDocumentLoadSuccess}>
        <Page pageNumber={pageNumber} width={650} />
      </Document>

      <p>
        Página {pageNumber} de {numPages || "?"}
      </p>

      <button onClick={() => handlePageChange(Math.max(1, pageNumber - 1))} disabled={pageNumber <= 1}>
        Anterior
      </button>
      <button
        onClick={() => handlePageChange(Math.min(numPages || pageNumber, pageNumber + 1))}
        disabled={!numPages || pageNumber >= numPages}
      >
        Próxima
      </button>
    </div>
  );
}

export default PdfViewer;