import { useState, useEffect, useRef, useCallback, useMemo } from "react";
import { Document, Page, pdfjs } from "react-pdf";
import "../styles/PdfViewer.css";
import "../styles/TextLayer.css";
import "../styles/AnnotationLayer.css";

pdfjs.GlobalWorkerOptions.workerSrc = "/pdf.worker.min.js";

const DEFAULT_PREFERENCES = {
  viewMode: "continuous",
  zoom: 1,
  fitWidth: true,
  visualMode: "normal",
  textMode: false,
  textSize: 18,
  lineHeight: 1.7,
  rotation: 0,
};

const VIEW_MODES = new Set(["continuous", "single"]);

function readStorage(key, fallback) {
  try {
    const storedValue = localStorage.getItem(key);
    if (!storedValue) return fallback;

    const parsedValue = JSON.parse(storedValue);
    if (Array.isArray(fallback)) {
      return Array.isArray(parsedValue) ? parsedValue : fallback;
    }

    return { ...fallback, ...parsedValue };
  } catch {
    return fallback;
  }
}

function escapeRegExp(value) {
  return value.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
}

function highlightText(text, query) {
  if (!query.trim()) return text;

  const parts = text.split(new RegExp(`(${escapeRegExp(query.trim())})`, "gi"));
  return parts.map((part, index) => (
    part.toLowerCase() === query.trim().toLowerCase()
      ? <mark key={`${part}-${index}`}>{part}</mark>
      : part
  ));
}

function getErrorMessage(error, fallback) {
  return error?.message || fallback;
}

// ---------------------------------------------------------------------------
// Props
// ---------------------------------------------------------------------------
// onSearchRequest  : async (query: string) => Array<{ page: number, excerpt: string }>
//                    Called when the user submits a search. The backend (Python)
//                    performs the full-document text search and returns results.
//
// onTextPageRequest: async (pageNumber: number) => string
//                    Called when text mode is active and the page changes.
//                    The backend extracts and returns the plain text for that page.
// ---------------------------------------------------------------------------

function PdfViewer({
  file,
  initialPage = 1,
  onPageChange,
  storageKey = "default",
  onSearchRequest,
  onTextPageRequest,
}) {
  const preferencesKey = `timerbook-pdf-preferences-${storageKey}`;
  const bookmarksKey = `timerbook-pdf-bookmarks-${storageKey}`;
  const savedPreferences = useMemo(() => readStorage(preferencesKey, DEFAULT_PREFERENCES), [preferencesKey]);
  const savedBookmarks = useMemo(() => readStorage(bookmarksKey, []), [bookmarksKey]);

  const [numPages, setNumPages] = useState(null);
  const [pageNumber, setPageNumber] = useState(initialPage);
  const [pagesRendered, setPagesRendered] = useState(0);
  const [viewMode, setViewMode] = useState(
    VIEW_MODES.has(savedPreferences.viewMode) ? savedPreferences.viewMode : DEFAULT_PREFERENCES.viewMode
  );
  const [zoom, setZoom] = useState(savedPreferences.zoom);
  const [fitWidth, setFitWidth] = useState(savedPreferences.fitWidth);
  const [visualMode, setVisualMode] = useState(savedPreferences.visualMode);
  const [textMode, setTextMode] = useState(savedPreferences.textMode);
  const [textSize, setTextSize] = useState(savedPreferences.textSize);
  const [lineHeight, setLineHeight] = useState(savedPreferences.lineHeight);
  const [rotation, setRotation] = useState(savedPreferences.rotation);
  const [bookmarks, setBookmarks] = useState(savedBookmarks);
  const [containerWidth, setContainerWidth] = useState(700);
  const [pageText, setPageText] = useState("");
  const [textLoading, setTextLoading] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");
  const [searchResults, setSearchResults] = useState([]);
  const [activeSearchIndex, setActiveSearchIndex] = useState(-1);
  const [searching, setSearching] = useState(false);
  const [searchError, setSearchError] = useState("");
  const containerRef = useRef(null);
  const viewportRef = useRef(null);
  const pageRefs = useRef([]);
  const observerRef = useRef(null);
  const searchInputRef = useRef(null);
  const isScrollingToPage = useRef(false);
  const hasScrolledToInitial = useRef(false);

  const onPageChangeRef = useRef(onPageChange);
  useEffect(() => { onPageChangeRef.current = onPageChange; }, [onPageChange]);

  // Notify parent whenever pageNumber changes
  useEffect(() => {
    onPageChangeRef.current?.(pageNumber);
  }, [pageNumber]);

  useEffect(() => {
    const preferences = {
      viewMode,
      zoom,
      fitWidth,
      visualMode,
      textMode,
      textSize,
      lineHeight,
      rotation,
    };
    localStorage.setItem(preferencesKey, JSON.stringify(preferences));
  }, [fitWidth, lineHeight, preferencesKey, rotation, textMode, textSize, viewMode, visualMode, zoom]);

  useEffect(() => {
    localStorage.setItem(bookmarksKey, JSON.stringify(bookmarks));
  }, [bookmarks, bookmarksKey]);

  // Reset scroll flag when file changes
  useEffect(() => {
    hasScrolledToInitial.current = false;
    const frame = requestAnimationFrame(() => {
      setPageNumber(initialPage);
      setPagesRendered(0);
    });

    return () => cancelAnimationFrame(frame);
  }, [file, initialPage]);

  useEffect(() => {
    const target = viewportRef.current;
    if (!target) return;

    const resizeObserver = new ResizeObserver(([entry]) => {
      setContainerWidth(entry.contentRect.width);
    });

    resizeObserver.observe(target);
    return () => resizeObserver.disconnect();
  }, []);

  // ---------------------------------------------------------------------------
  // Text mode — delegate extraction to the backend via onTextPageRequest
  // ---------------------------------------------------------------------------
  useEffect(() => {
    let cancelled = false;

    const loadPageText = async () => {
      if (!textMode || !onTextPageRequest) {
        setPageText("");
        return;
      }

      setTextLoading(true);
      try {
        const text = await onTextPageRequest(pageNumber);
        if (!cancelled) setPageText(text || "Nenhum texto extraível nesta página.");
      } catch (error) {
        console.error("Erro ao obter texto da página:", error);
        if (!cancelled) setPageText(getErrorMessage(error, "Não foi possível obter o texto desta página."));
      } finally {
        if (!cancelled) setTextLoading(false);
      }
    };

    loadPageText();
    return () => { cancelled = true; };
  }, [textMode, pageNumber, onTextPageRequest]);

  // Scroll to initialPage once enough pages have rendered.
  useEffect(() => {
    if (viewMode !== "continuous") return;
    if (initialPage <= 1 || hasScrolledToInitial.current) return;
    if (pagesRendered < initialPage) return;

    const target = pageRefs.current[initialPage - 1];
    if (!target) return;

    const ro = new ResizeObserver(() => {
      if (target.offsetHeight > 0) {
        ro.disconnect();
        hasScrolledToInitial.current = true;
        isScrollingToPage.current = true;

        target.scrollIntoView({ behavior: "instant", block: "start" });

        setTimeout(() => { isScrollingToPage.current = false; }, 600);
      }
    });

    ro.observe(target);
    return () => ro.disconnect();
  }, [pagesRendered, initialPage, viewMode]);

  // IntersectionObserver: track which page is most visible
  useEffect(() => {
    if (!numPages || viewMode !== "continuous") return;

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
        root: viewportRef.current,
        threshold: Array.from({ length: 21 }, (_, i) => i * 0.05),
      }
    );

    pageRefs.current.forEach((el) => {
      if (el) observerRef.current.observe(el);
    });

    return () => observerRef.current?.disconnect();
  }, [numPages, viewMode]);

  const onDocumentLoadSuccess = useCallback(({ numPages: total }) => {
    setNumPages(total);
    pageRefs.current = new Array(total).fill(null);
  }, []);

  const handlePageRenderSuccess = useCallback((pageIndex) => {
    setPagesRendered((prev) => Math.max(prev, pageIndex + 1));
  }, []);

  const scrollToPage = useCallback((targetPage) => {
    if (viewMode !== "continuous") return;
    const target = pageRefs.current[targetPage - 1];
    if (!target) return;

    isScrollingToPage.current = true;
    target.scrollIntoView({ behavior: "smooth", block: "start" });
    setTimeout(() => { isScrollingToPage.current = false; }, 500);
  }, [viewMode]);

  const goToPage = useCallback((targetPage) => {
    if (!numPages) return;
    const nextPage = Math.min(numPages, Math.max(1, targetPage));
    setPageNumber(nextPage);
    scrollToPage(nextPage);
  }, [numPages, scrollToPage]);

  const goToSearchResult = useCallback((resultIndex) => {
    if (!searchResults.length) return;

    const nextIndex = (resultIndex + searchResults.length) % searchResults.length;
    setActiveSearchIndex(nextIndex);
    goToPage(searchResults[nextIndex].page);
  }, [goToPage, searchResults]);

  // ---------------------------------------------------------------------------
  // Search — delegate to the backend via onSearchRequest
  // ---------------------------------------------------------------------------
  const handleSearch = async () => {
    const query = searchQuery.trim();
    if (!query || !onSearchRequest) {
      setSearchResults([]);
      setActiveSearchIndex(-1);
      return;
    }

    setSearching(true);
    setSearchError("");
    try {
      // onSearchRequest returns Array<{ page: number, excerpt: string }>
      const results = await onSearchRequest(query);
      setSearchResults(results);
      setActiveSearchIndex(results.length ? 0 : -1);
      if (results.length) goToPage(results[0].page);
    } catch (error) {
      console.error("Erro ao buscar no PDF:", error);
      setSearchResults([]);
      setActiveSearchIndex(-1);
      setSearchError(getErrorMessage(error, "Não foi possível buscar no PDF."));
    } finally {
      setSearching(false);
    }
  };

  const toggleBookmark = () => {
    setBookmarks((currentBookmarks) => (
      currentBookmarks.includes(pageNumber)
        ? currentBookmarks.filter((page) => page !== pageNumber)
        : [...currentBookmarks, pageNumber].sort((a, b) => a - b)
    ));
  };

  const handleViewModeChange = (mode) => {
    if (!VIEW_MODES.has(mode)) return;

    pageRefs.current = new Array(numPages || 0).fill(null);
    setPagesRendered(0);
    setViewMode(mode);

    if (mode === "continuous") {
      requestAnimationFrame(() => {
        const target = pageRefs.current[pageNumber - 1];
        target?.scrollIntoView({ behavior: "smooth", block: "start" });
      });
    }
  };

  const handlePageInputChange = (event) => {
    const nextPage = Number(event.target.value);
    if (!Number.isFinite(nextPage)) return;
    goToPage(nextPage);
  };

  const handleKeyDown = (event) => {
    if (event.target.tagName === "INPUT") return;

    if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === "f") {
      event.preventDefault();
      searchInputRef.current?.focus();
      return;
    }

    if (event.key === "/") {
      event.preventDefault();
      searchInputRef.current?.focus();
      return;
    }

    if (event.key.toLowerCase() === "b") {
      event.preventDefault();
      toggleBookmark();
      return;
    }

    if (event.key.toLowerCase() === "t") {
      event.preventDefault();
      setTextMode((value) => !value);
      return;
    }

    if (event.key === "+") {
      event.preventDefault();
      setZoom((value) => Math.min(1.8, value + 0.1));
      return;
    }

    if (event.key === "-") {
      event.preventDefault();
      setZoom((value) => Math.max(0.7, value - 0.1));
      return;
    }

    if (event.key === "ArrowLeft" || event.key === "PageUp") {
      event.preventDefault();
      goToPage(pageNumber - 1);
    }

    if (event.key === "ArrowRight" || event.key === "PageDown" || event.key === " ") {
      event.preventDefault();
      goToPage(pageNumber + 1);
    }

    if (event.key === "Home") {
      event.preventDefault();
      goToPage(1);
    }

    if (event.key === "End" && numPages) {
      event.preventDefault();
      goToPage(numPages);
    }
  };

  const baseWidth = fitWidth ? Math.min(900, Math.max(320, containerWidth - 48)) : 650;
  const pageWidth = Math.round(baseWidth * zoom);
  const isCurrentPageBookmarked = bookmarks.includes(pageNumber);
  const activeResult = activeSearchIndex >= 0 ? searchResults[activeSearchIndex] : null;
  const safeViewMode = VIEW_MODES.has(viewMode) ? viewMode : DEFAULT_PREFERENCES.viewMode;
  const pagesToRender = safeViewMode === "single" && numPages
    ? [pageNumber]
    : numPages
      ? Array.from({ length: numPages }, (_, i) => i + 1)
      : [];

  return (
    <div
      className={`pdf-viewer pdf-visual-${visualMode}`}
      onKeyDown={handleKeyDown}
    >
      <div className="pdf-toolbar" role="toolbar" aria-label="Controles do leitor de PDF">
        <div className="pdf-toolbar-group" aria-label="Navegação de páginas">
          <button type="button" onClick={() => goToPage(pageNumber - 1)} disabled={pageNumber <= 1} aria-label="Página anterior">
            {"<"}
          </button>
          <label className="pdf-page-jump">
            <span>Página</span>
            <input
              type="number"
              min="1"
              max={numPages || 1}
              value={pageNumber}
              onChange={handlePageInputChange}
              aria-label="Ir para página"
            />
            <span aria-live="polite">de {numPages || "..."}</span>
          </label>
          <button type="button" onClick={() => goToPage(pageNumber + 1)} disabled={!numPages || pageNumber >= numPages} aria-label="Próxima página">
            {">"}
          </button>
        </div>

        <div className="pdf-toolbar-group" aria-label="Modo de leitura">
          <button
            type="button"
            className={safeViewMode === "continuous" ? "active" : ""}
            onClick={() => handleViewModeChange("continuous")}
            aria-pressed={safeViewMode === "continuous"}
          >
            Rolagem
          </button>
          <button
            type="button"
            className={safeViewMode === "single" ? "active" : ""}
            onClick={() => handleViewModeChange("single")}
            aria-pressed={safeViewMode === "single"}
          >
            Página
          </button>
        </div>

        <form
          className="pdf-toolbar-group pdf-search"
          aria-label="Busca no PDF"
          onSubmit={(event) => {
            event.preventDefault();
            handleSearch();
          }}
        >
          <input
            ref={searchInputRef}
            type="search"
            value={searchQuery}
            onChange={(event) => {
              setSearchQuery(event.target.value);
              if (searchError) setSearchError("");
            }}
            placeholder="Buscar"
            aria-label="Buscar no PDF"
          />
          <button type="submit" disabled={searching || !onSearchRequest}>
            {searching ? "..." : "Ir"}
          </button>
          <button
            type="button"
            onClick={() => goToSearchResult(activeSearchIndex - 1)}
            disabled={!searchResults.length}
            aria-label="Resultado anterior"
          >
            {"<"}
          </button>
          <button
            type="button"
            onClick={() => goToSearchResult(activeSearchIndex + 1)}
            disabled={!searchResults.length}
            aria-label="Próximo resultado"
          >
            {">"}
          </button>
          <span className="pdf-search-count" aria-live="polite">
            {searchResults.length ? `${activeSearchIndex + 1}/${searchResults.length}` : "0"}
          </span>
        </form>

        <div className="pdf-toolbar-group" aria-label="Zoom">
          <button type="button" onClick={() => setZoom((value) => Math.max(0.7, value - 0.1))} aria-label="Diminuir zoom">
            -
          </button>
          <span className="pdf-zoom-label" aria-live="polite">{Math.round(zoom * 100)}%</span>
          <button type="button" onClick={() => setZoom((value) => Math.min(1.8, value + 0.1))} aria-label="Aumentar zoom">
            +
          </button>
          <button
            type="button"
            className={fitWidth ? "active" : ""}
            onClick={() => setFitWidth((value) => !value)}
            aria-pressed={fitWidth}
          >
            Ajustar
          </button>
          <button type="button" onClick={() => setRotation((value) => (value + 90) % 360)} aria-label="Girar página">
            Girar
          </button>
        </div>

        <div className="pdf-toolbar-group" aria-label="Acessibilidade visual">
          <select
            value={visualMode}
            onChange={(event) => setVisualMode(event.target.value)}
            aria-label="Modo visual"
          >
            <option value="normal">Normal</option>
            <option value="contrast">Contraste</option>
            <option value="invert">Inverter</option>
            <option value="grayscale">Cinza</option>
          </select>
          <button
            type="button"
            className={textMode ? "active" : ""}
            onClick={() => setTextMode((value) => !value)}
            aria-pressed={textMode}
            disabled={!onTextPageRequest}
          >
            Texto
          </button>
          <button
            type="button"
            className={isCurrentPageBookmarked ? "active" : ""}
            onClick={toggleBookmark}
            aria-pressed={isCurrentPageBookmarked}
          >
            Marcar
          </button>
        </div>
      </div>

      {(bookmarks.length > 0 || activeResult || searchError) && (
        <div className="pdf-secondary-bar">
          {bookmarks.length > 0 && (
            <div className="pdf-bookmarks" aria-label="Marcadores">
              <span>Marcadores</span>
              {bookmarks.map((page) => (
                <button type="button" key={page} onClick={() => goToPage(page)}>
                  {page}
                </button>
              ))}
            </div>
          )}
          {activeResult && (
            <button type="button" className="pdf-search-excerpt" onClick={() => goToPage(activeResult.page)}>
              p. {activeResult.page}: {activeResult.excerpt}
            </button>
          )}
          {searchError && <span className="pdf-search-error">{searchError}</span>}
        </div>
      )}

      <div
        ref={viewportRef}
        className={`pdf-viewport pdf-viewport-${safeViewMode}`}
        tabIndex={0}
        aria-label="Área de leitura do PDF"
      >
        {textMode ? (
          <section className="pdf-text-reader" aria-label={`Texto extraído da página ${pageNumber}`}>
            <div className="pdf-text-toolbar">
              <span>Texto da página {pageNumber}</span>
              <button type="button" onClick={() => setTextSize((value) => Math.max(14, value - 1))}>A-</button>
              <button type="button" onClick={() => setTextSize((value) => Math.min(28, value + 1))}>A+</button>
              <button type="button" onClick={() => setLineHeight((value) => Math.max(1.3, Number((value - 0.1).toFixed(1))))}>Espaço -</button>
              <button type="button" onClick={() => setLineHeight((value) => Math.min(2.2, Number((value + 0.1).toFixed(1))))}>Espaço +</button>
            </div>
            <article
              className="pdf-text-page"
              style={{ fontSize: `${textSize}px`, lineHeight }}
            >
              {textLoading ? "Carregando texto..." : highlightText(pageText, searchQuery)}
            </article>
          </section>
        ) : (
          <div ref={containerRef} className="pdf-pages">
            <Document
              file={file}
              onLoadSuccess={onDocumentLoadSuccess}
              onLoadError={(err) => console.error("ERRO AO CARREGAR PDF:", err)}
              onSourceError={(err) => console.error("ERRO NA SOURCE:", err)}
              loading={<p className="pdf-message">Carregando PDF...</p>}
              error={<p className="pdf-message pdf-message-error">Erro ao carregar PDF</p>}
            >
              {pagesToRender.map((renderedPage) => {
                const pageIndex = renderedPage - 1;
                return (
                  <div
                    key={renderedPage}
                    className="pdf-page-shell"
                    data-page-index={pageIndex}
                    ref={(el) => (pageRefs.current[pageIndex] = el)}
                    aria-label={`Página ${renderedPage}`}
                  >
                    <Page
                      pageNumber={renderedPage}
                      width={pageWidth}
                      rotate={rotation}
                      renderTextLayer
                      renderAnnotationLayer
                      onRenderSuccess={() => handlePageRenderSuccess(pageIndex)}
                    />
                  </div>
                );
              })}
            </Document>
          </div>
        )}
      </div>
    </div>
  );
}

export default PdfViewer;
