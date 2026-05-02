import { useState, useEffect, useRef, useCallback } from "react";
import { Document, Page, pdfjs } from "react-pdf";
import "../styles/TextLayer.css";
import "../styles/AnnotationLayer.css";

pdfjs.GlobalWorkerOptions.workerSrc = "/pdf.worker.min.js";

function PdfViewer({ file, initialPage = 1, onPageChange }) {
  const [numPages, setNumPages] = useState(null);
  const [pageNumber, setPageNumber] = useState(initialPage);
  const [pagesRendered, setPagesRendered] = useState(0);
  const containerRef = useRef(null);
  const pageRefs = useRef([]);
  const observerRef = useRef(null);
  const isScrollingToPage = useRef(false);
  const hasScrolledToInitial = useRef(false);

  const onPageChangeRef = useRef(onPageChange);
  useEffect(() => { onPageChangeRef.current = onPageChange; }, [onPageChange]);

  // Notify parent whenever pageNumber changes
  useEffect(() => {
    onPageChangeRef.current?.(pageNumber);
  }, [pageNumber]);

  // Reset scroll flag when file changes
  useEffect(() => {
    hasScrolledToInitial.current = false;
    setPageNumber(initialPage);
    setPagesRendered(0);
  }, [file, initialPage]);

  // Scroll to initialPage once enough pages have rendered.
  // We wait until the target page itself has rendered (pagesRendered >= initialPage)
  // AND has a non-zero offsetHeight, meaning its canvas is painted.
  useEffect(() => {
    if (initialPage <= 1 || hasScrolledToInitial.current) return;
    if (pagesRendered < initialPage) return;

    const target = pageRefs.current[initialPage - 1];
    if (!target) return;

    // Use a ResizeObserver on the target page to detect when it gains real height
    // (canvas painted) and then scroll. Disconnect immediately after first scroll.
    const ro = new ResizeObserver(() => {
      if (target.offsetHeight > 0) {
        ro.disconnect();
        hasScrolledToInitial.current = true;
        isScrollingToPage.current = true;

        // scrollIntoView positions relative to the scrollable ancestor
        target.scrollIntoView({ behavior: "instant", block: "start" });

        // Keep the flag on long enough to suppress the IntersectionObserver
        setTimeout(() => { isScrollingToPage.current = false; }, 600);
      }
    });

    ro.observe(target);
    return () => ro.disconnect();
  }, [pagesRendered, initialPage]);

  // IntersectionObserver: track which page is most visible
  useEffect(() => {
    if (!numPages) return;

    observerRef.current?.disconnect();
    const visibilityMap = {};

    observerRef.current = new IntersectionObserver(
      (entries) => {
        if (isScrollingToPage.current) return;

        entries.forEach((entry) => {
          const index = Number(entry.target.dataset.pageIndex);
          visibilityMap[index] = entry.intersectionRatio;
        });

        const mostVisible = Object.entries(visibilityMap).reduce(
          (best, [idx, ratio]) => (ratio > best.ratio ? { idx: Number(idx), ratio } : best),
          { idx: 0, ratio: -1 }
        );

        if (mostVisible.ratio > 0) {
          const newPage = mostVisible.idx + 1;
          setPageNumber((prev) => (prev !== newPage ? newPage : prev));
        }
      },
      {
        root: containerRef.current,
        threshold: Array.from({ length: 21 }, (_, i) => i * 0.05),
      }
    );

    pageRefs.current.forEach((el) => {
      if (el) observerRef.current.observe(el);
    });

    return () => observerRef.current?.disconnect();
  }, [numPages]);

  const onDocumentLoadSuccess = useCallback(({ numPages }) => {
    setNumPages(numPages);
    pageRefs.current = new Array(numPages).fill(null);
  }, []);

  // Called by each Page when it finishes rendering its canvas
  const handlePageRenderSuccess = useCallback((pageIndex) => {
    setPagesRendered((prev) => Math.max(prev, pageIndex + 1));
  }, []);

  return (
    <div
      ref={containerRef}
      style={{
        width: 700,
        margin: "0 auto",
        height: "100%",
        overflowY: "auto",
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        gap: "12px",
        paddingBottom: "32px",
      }}
    >
      <Document
        file={file}
        onLoadSuccess={onDocumentLoadSuccess}
        onLoadError={(err) => console.error("ERRO AO CARREGAR PDF:", err)}
        onSourceError={(err) => console.error("ERRO NA SOURCE:", err)}
        loading={<p style={{ color: "#9ca3af", marginTop: "2rem" }}>Carregando PDF...</p>}
        error={<p style={{ color: "#f87171", marginTop: "2rem" }}>Erro ao carregar PDF</p>}
      >
        {numPages &&
          Array.from({ length: numPages }, (_, i) => (
            <div
              key={i}
              data-page-index={i}
              ref={(el) => (pageRefs.current[i] = el)}
              style={{ marginBottom: "8px" }}
            >
              <Page
                pageNumber={i + 1}
                width={650}
                onRenderSuccess={() => handlePageRenderSuccess(i)}
              />
            </div>
          ))}
      </Document>
    </div>
  );
}

export default PdfViewer;