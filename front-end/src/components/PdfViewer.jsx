import { useState, useEffect, useRef, useCallback, useMemo } from "react";
import {
  AreaHighlight,
  DrawingHighlight,
  FreetextHighlight,
  MonitoredHighlightContainer,
  PdfHighlighter,
  PdfLoader,
  ShapeHighlight,
  TextHighlight,
  useHighlightContainerContext,
  usePdfHighlighterContext,
} from "react-pdf-highlighter-plus";
import pdfWorkerUrl from "pdfjs-dist/build/pdf.worker.min.mjs?url";
import "react-pdf-highlighter-plus/style/style.css";
import "../styles/PdfViewer.css";

const DEFAULT_PREFERENCES = {
  viewMode: "continuous",
  zoom: 1,
  fitWidth: true,
  visualMode: "normal",
  textMode: false,
  textSize: 18,
  lineHeight: 1.7,
  rotation: 0,
  annotationMode: "select",
};

const VIEW_MODES = new Set(["continuous", "single"]);
const ANNOTATION_MODES = new Set(["select", "area", "note", "draw", "shape"]);
const HIGHLIGHT_COLORS = ["rgba(255, 226, 143, 0.85)", "#ffcdd2", "#c8e6c9", "#bbdefb", "#e1bee7"];
const NOTE_BACKGROUNDS = ["#ffffc8", "#ffcdd2", "#c8e6c9", "#bbdefb", "#e1bee7"];
const NOTE_TEXT_COLORS = ["#333333", "#d32f2f", "#1976d2", "#388e3c", "#7b1fa2"];

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

function createAnnotationId() {
  if (crypto?.randomUUID) return crypto.randomUUID();
  return `${Date.now()}-${Math.random().toString(16).slice(2)}`;
}

function PdfAnnotationContainer({ editHighlight, removeHighlight }) {
  const {
    highlight,
    viewportToScaled,
    screenshot,
    isScrolledTo,
    highlightBindings,
  } = useHighlightContainerContext();
  const { toggleEditInProgress } = usePdfHighlighterContext();

  const updatePosition = (rect, extraContent = {}) => {
    editHighlight(highlight.id, {
      position: {
        boundingRect: viewportToScaled(rect),
        rects: highlight.position.rects?.map(viewportToScaled) || [],
      },
      ...extraContent,
    });
  };

  const commonProps = {
    highlight,
    isScrolledTo,
    bounds: highlightBindings.textLayer,
    onDelete: () => removeHighlight(highlight.id),
  };

  let renderedHighlight = null;

  if (highlight.type === "area") {
    renderedHighlight = (
      <AreaHighlight
        {...commonProps}
        onChange={(rect) => updatePosition(rect, { content: { image: screenshot(rect) } })}
        onEditStart={() => toggleEditInProgress(true)}
        highlightColor={highlight.highlightColor || HIGHLIGHT_COLORS[0]}
        onStyleChange={(style) => editHighlight(highlight.id, style)}
        copyText={highlight.content?.text}
        colorPresets={HIGHLIGHT_COLORS}
      />
    );
  } else if (highlight.type === "freetext") {
    renderedHighlight = (
      <FreetextHighlight
        {...commonProps}
        compact
        onChange={(rect) => updatePosition(rect)}
        onTextChange={(text) => editHighlight(highlight.id, { content: { ...highlight.content, text } })}
        onStyleChange={(style) => editHighlight(highlight.id, style)}
        onEditStart={() => toggleEditInProgress(true)}
        onEditEnd={() => toggleEditInProgress(false)}
        color={highlight.color || "#333333"}
        backgroundColor={highlight.backgroundColor || "#ffffc8"}
        fontSize={highlight.fontSize || "14px"}
        backgroundColorPresets={NOTE_BACKGROUNDS}
        textColorPresets={NOTE_TEXT_COLORS}
      />
    );
  } else if (highlight.type === "drawing") {
    renderedHighlight = (
      <DrawingHighlight
        {...commonProps}
        onChange={(rect) => updatePosition(rect)}
        onStyleChange={(image, strokes) => editHighlight(highlight.id, { content: { ...highlight.content, image, strokes } })}
        onEditStart={() => toggleEditInProgress(true)}
        onEditEnd={() => toggleEditInProgress(false)}
      />
    );
  } else if (highlight.type === "shape") {
    renderedHighlight = (
      <ShapeHighlight
        {...commonProps}
        onChange={(rect) => updatePosition(rect)}
        onStyleChange={(style) => editHighlight(highlight.id, { content: { ...highlight.content, shape: { ...highlight.content?.shape, ...style } } })}
        onEditStart={() => toggleEditInProgress(true)}
        onEditEnd={() => toggleEditInProgress(false)}
        shapeType={highlight.content?.shape?.shapeType || "rectangle"}
        strokeColor={highlight.content?.shape?.strokeColor || "#1976d2"}
        strokeWidth={highlight.content?.shape?.strokeWidth || 2}
        startPoint={highlight.content?.shape?.startPoint}
        endPoint={highlight.content?.shape?.endPoint}
        colorPresets={["#1976d2", "#d32f2f", "#388e3c", "#7b1fa2", "#111827"]}
      />
    );
  } else {
    renderedHighlight = (
      <TextHighlight
        highlight={highlight}
        isScrolledTo={isScrolledTo}
        highlightColor={highlight.highlightColor || HIGHLIGHT_COLORS[0]}
        highlightStyle={highlight.highlightStyle || "highlight"}
        onStyleChange={(style) => editHighlight(highlight.id, style)}
        onDelete={() => removeHighlight(highlight.id)}
        copyText={highlight.content?.text}
        colorPresets={HIGHLIGHT_COLORS}
      />
    );
  }

  return (
    <MonitoredHighlightContainer
      highlightTip={{
        position: highlight.position,
        content: (
          <div className="pdf-annotation-tip">
            <strong>{highlight.type === "freetext" ? "Nota" : "Anotação"}</strong>
            {highlight.content?.text && <span>{highlight.content.text}</span>}
          </div>
        ),
      }}
    >
      {renderedHighlight}
    </MonitoredHighlightContainer>
  );
}

function PdfHighlighterContent({
  annotationMode,
  annotations,
  drawingStrokeWidth,
  editHighlight,
  handleDrawingComplete,
  handleFreetextClick,
  handleSelection,
  handleShapeComplete,
  loadedPdfDocument,
  pdfScaleValue,
  removeHighlight,
  safeViewMode,
  setAnnotationMode,
  setNumPages,
  setPdfDocument,
  shapeType,
  visualMode,
  highlighterUtilsRef,
}) {
  useEffect(() => {
    setPdfDocument(loadedPdfDocument);
    setNumPages(loadedPdfDocument.numPages);
  }, [loadedPdfDocument, setNumPages, setPdfDocument]);

  return (
    <PdfHighlighter
      pdfDocument={loadedPdfDocument}
      highlights={annotations}
      pdfScaleValue={pdfScaleValue}
      utilsRef={(utils) => {
        highlighterUtilsRef.current = utils;
      }}
      onSelection={handleSelection}
      enableAreaSelection={(event) => annotationMode === "area" || event.altKey}
      areaSelectionMode={annotationMode === "area"}
      enableFreetextCreation={() => annotationMode === "note"}
      onFreetextClick={handleFreetextClick}
      enableDrawingMode={annotationMode === "draw"}
      onDrawingComplete={handleDrawingComplete}
      onDrawingCancel={() => setAnnotationMode("select")}
      enableShapeMode={shapeType}
      onShapeComplete={handleShapeComplete}
      onShapeCancel={() => setAnnotationMode("select")}
      drawingStrokeColor="#1976d2"
      drawingStrokeWidth={drawingStrokeWidth}
      shapeStrokeColor="#1976d2"
      shapeStrokeWidth={2}
      textSelectionColor="rgba(255, 226, 143, 0.35)"
      theme={{
        mode: visualMode === "invert" ? "dark" : "light",
        containerBackgroundColor: "transparent",
      }}
      style={{ height: safeViewMode === "single" ? "100%" : "100%" }}
    >
      <PdfAnnotationContainer editHighlight={editHighlight} removeHighlight={removeHighlight} />
    </PdfHighlighter>
  );
}

function PdfViewer({ file, initialPage = 1, onPageChange, storageKey = "default" }) {
  const preferencesKey = `timerbook-pdf-preferences-${storageKey}`;
  const bookmarksKey = `timerbook-pdf-bookmarks-${storageKey}`;
  const annotationsKey = `timerbook-pdf-annotations-${storageKey}`;
  const savedPreferences = useMemo(() => readStorage(preferencesKey, DEFAULT_PREFERENCES), [preferencesKey]);
  const savedBookmarks = useMemo(() => readStorage(bookmarksKey, []), [bookmarksKey]);
  const savedAnnotations = useMemo(() => readStorage(annotationsKey, []), [annotationsKey]);

  const [numPages, setNumPages] = useState(null);
  const [pageNumber, setPageNumber] = useState(initialPage);
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
  const [annotationMode, setAnnotationMode] = useState(
    ANNOTATION_MODES.has(savedPreferences.annotationMode) ? savedPreferences.annotationMode : DEFAULT_PREFERENCES.annotationMode
  );
  const [bookmarks, setBookmarks] = useState(savedBookmarks);
  const [annotations, setAnnotations] = useState(savedAnnotations);
  const [pdfData, setPdfData] = useState(null);
  const [pdfDocument, setPdfDocument] = useState(null);
  const [pageText, setPageText] = useState("");
  const [textLoading, setTextLoading] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");
  const [searchResults, setSearchResults] = useState([]);
  const [activeSearchIndex, setActiveSearchIndex] = useState(-1);
  const [searching, setSearching] = useState(false);
  const viewportRef = useRef(null);
  const searchInputRef = useRef(null);
  const pageTextCacheRef = useRef(new Map());
  const highlighterUtilsRef = useRef(null);
  const isCurrentPageBookmarked = bookmarks.includes(pageNumber);
  const activeResult = activeSearchIndex >= 0 ? searchResults[activeSearchIndex] : null;
  const safeViewMode = VIEW_MODES.has(viewMode) ? viewMode : DEFAULT_PREFERENCES.viewMode;
  const pdfScaleValue = fitWidth ? "page-width" : Number(zoom.toFixed(2));
  const shapeType = annotationMode === "shape" ? "rectangle" : null;

  const onPageChangeRef = useRef(onPageChange);
  useEffect(() => { onPageChangeRef.current = onPageChange; }, [onPageChange]);

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
      annotationMode,
    };
    localStorage.setItem(preferencesKey, JSON.stringify(preferences));
  }, [annotationMode, fitWidth, lineHeight, preferencesKey, rotation, textMode, textSize, viewMode, visualMode, zoom]);

  useEffect(() => {
    localStorage.setItem(bookmarksKey, JSON.stringify(bookmarks));
  }, [bookmarks, bookmarksKey]);

  useEffect(() => {
    localStorage.setItem(annotationsKey, JSON.stringify(annotations));
  }, [annotations, annotationsKey]);

  useEffect(() => {
    let cancelled = false;

    const loadData = async () => {
      if (!file) {
        setPdfData(null);
        setPdfDocument(null);
        return;
      }

      try {
        const arrayBuffer = await file.arrayBuffer();
        if (!cancelled) {
          pageTextCacheRef.current = new Map();
          setPdfData(new Uint8Array(arrayBuffer));
          setPageNumber(initialPage);
        }
      } catch (error) {
        console.error("Erro ao preparar PDF:", error);
        if (!cancelled) setPdfData(null);
      }
    };

    loadData();
    return () => {
      cancelled = true;
    };
  }, [file, initialPage]);

  useEffect(() => {
    const eventBus = highlighterUtilsRef.current?.getEventBus?.();
    if (!eventBus) return undefined;

    const handlePageChanging = (event) => {
      if (event?.pageNumber) setPageNumber(event.pageNumber);
    };

    eventBus.on("pagechanging", handlePageChanging);
    return () => eventBus.off("pagechanging", handlePageChanging);
  }, [pdfDocument]);

  useEffect(() => {
    const viewer = highlighterUtilsRef.current?.getViewer?.();
    if (!viewer) return;

    if (typeof viewer.pagesRotation === "number") {
      viewer.pagesRotation = rotation;
      viewer.update?.();
    }
  }, [rotation, pdfDocument]);

  useEffect(() => {
    const updateVisiblePages = () => {
      const pages = viewportRef.current?.querySelectorAll(".pdfViewer .page");
      pages?.forEach((page) => {
        const pageElement = page;
        pageElement.style.display = safeViewMode === "single" && pageElement.dataset.pageNumber !== String(pageNumber)
          ? "none"
          : "";
      });
    };

    updateVisiblePages();
    const timeout = setTimeout(updateVisiblePages, 150);
    return () => clearTimeout(timeout);
  }, [annotations, pageNumber, pdfDocument, pdfScaleValue, safeViewMode]);

  const extractPageText = useCallback(async (targetPage) => {
    if (!pdfDocument) return "";
    if (pageTextCacheRef.current.has(targetPage)) {
      return pageTextCacheRef.current.get(targetPage);
    }

    const page = await pdfDocument.getPage(targetPage);
    const textContent = await page.getTextContent();
    const extractedText = textContent.items.map((item) => item.str).join(" ");
    pageTextCacheRef.current.set(targetPage, extractedText);
    return extractedText;
  }, [pdfDocument]);

  useEffect(() => {
    let cancelled = false;

    const loadPageText = async () => {
      if (!textMode || !pdfDocument) {
        setPageText("");
        return;
      }

      setTextLoading(true);
      try {
        const extractedText = await extractPageText(pageNumber);
        if (!cancelled) setPageText(extractedText || "Nenhum texto extraivel nesta pagina.");
      } catch (error) {
        console.error("Erro ao extrair texto da pagina:", error);
        if (!cancelled) setPageText("Nao foi possivel extrair o texto desta pagina.");
      } finally {
        if (!cancelled) setTextLoading(false);
      }
    };

    loadPageText();
    return () => {
      cancelled = true;
    };
  }, [extractPageText, pageNumber, pdfDocument, textMode]);

  const goToPage = useCallback((targetPage) => {
    if (!numPages) return;
    const nextPage = Math.min(numPages, Math.max(1, targetPage));
    setPageNumber(nextPage);
    highlighterUtilsRef.current?.goToPage?.(nextPage);
  }, [numPages]);

  const goToSearchResult = useCallback((resultIndex) => {
    if (!searchResults.length) return;

    const nextIndex = (resultIndex + searchResults.length) % searchResults.length;
    setActiveSearchIndex(nextIndex);
    goToPage(searchResults[nextIndex].page);
    highlighterUtilsRef.current?.search?.(searchQuery, { highlightAll: true });
  }, [goToPage, searchQuery, searchResults]);

  const handleSearch = async () => {
    const query = searchQuery.trim();
    if (!query || !pdfDocument) {
      highlighterUtilsRef.current?.clearSearch?.();
      setSearchResults([]);
      setActiveSearchIndex(-1);
      return;
    }

    setSearching(true);
    try {
      const results = [];
      const normalizedQuery = query.toLowerCase();

      for (let page = 1; page <= pdfDocument.numPages; page += 1) {
        const text = await extractPageText(page);
        const normalizedText = text.toLowerCase();
        const matchIndex = normalizedText.indexOf(normalizedQuery);

        if (matchIndex >= 0) {
          const excerptStart = Math.max(0, matchIndex - 45);
          const excerptEnd = Math.min(text.length, matchIndex + query.length + 75);
          results.push({
            page,
            excerpt: text.slice(excerptStart, excerptEnd).trim(),
          });
        }
      }

      highlighterUtilsRef.current?.search?.(query, { highlightAll: true });
      setSearchResults(results);
      setActiveSearchIndex(results.length ? 0 : -1);
      if (results.length) goToPage(results[0].page);
    } catch (error) {
      console.error("Erro ao buscar no PDF:", error);
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
    setViewMode(mode);
    if (mode === "single") goToPage(pageNumber);
  };

  const handlePageInputChange = (event) => {
    const nextPage = Number(event.target.value);
    if (!Number.isFinite(nextPage)) return;
    goToPage(nextPage);
  };

  const addHighlight = useCallback((highlight) => {
    setAnnotations((currentAnnotations) => [{ ...highlight, id: createAnnotationId() }, ...currentAnnotations]);
  }, []);

  const editHighlight = useCallback((id, edit) => {
    setAnnotations((currentAnnotations) => (
      currentAnnotations.map((highlight) => (
        highlight.id === id
          ? {
              ...highlight,
              ...edit,
              content: edit.content ? { ...highlight.content, ...edit.content } : highlight.content,
            }
          : highlight
      ))
    ));
  }, []);

  const removeHighlight = useCallback((id) => {
    setAnnotations((currentAnnotations) => currentAnnotations.filter((highlight) => highlight.id !== id));
  }, []);

  const handleSelection = useCallback((selection) => {
    const ghostHighlight = selection.makeGhostHighlight();
    addHighlight({
      ...ghostHighlight,
      type: ghostHighlight.type || (ghostHighlight.content?.image ? "area" : "text"),
      highlightColor: HIGHLIGHT_COLORS[0],
    });
  }, [addHighlight]);

  const handleFreetextClick = useCallback((position) => {
    addHighlight({
      type: "freetext",
      position,
      content: { text: "Nova nota" },
      color: "#333333",
      backgroundColor: "#ffffc8",
      fontSize: "14px",
    });
    setAnnotationMode("select");
  }, [addHighlight]);

  const handleDrawingComplete = useCallback((image, position, strokes) => {
    addHighlight({
      type: "drawing",
      position,
      content: { image, strokes },
    });
    setAnnotationMode("select");
  }, [addHighlight]);

  const handleShapeComplete = useCallback((position, shape) => {
    addHighlight({
      type: "shape",
      position,
      content: { shape },
    });
    setAnnotationMode("select");
  }, [addHighlight]);

  const handleKeyDown = (event) => {
    if (event.target.tagName === "INPUT" || event.target.tagName === "TEXTAREA") return;

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
            onChange={(event) => setSearchQuery(event.target.value)}
            placeholder="Buscar"
            aria-label="Buscar no PDF"
          />
          <button type="submit" disabled={searching || !pdfDocument}>
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

        <div className="pdf-toolbar-group" aria-label="Anotações">
          <button type="button" className={annotationMode === "select" ? "active" : ""} onClick={() => setAnnotationMode("select")}>
            Seleção
          </button>
          <button type="button" className={annotationMode === "area" ? "active" : ""} onClick={() => setAnnotationMode("area")}>
            Área
          </button>
          <button type="button" className={annotationMode === "note" ? "active" : ""} onClick={() => setAnnotationMode("note")}>
            Nota
          </button>
          <button type="button" className={annotationMode === "draw" ? "active" : ""} onClick={() => setAnnotationMode("draw")}>
            Desenho
          </button>
          <button type="button" className={annotationMode === "shape" ? "active" : ""} onClick={() => setAnnotationMode("shape")}>
            Forma
          </button>
        </div>
      </div>

      {(bookmarks.length > 0 || activeResult || annotations.length > 0 || annotationMode !== "select") && (
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
          {annotations.length > 0 && (
            <div className="pdf-bookmarks" aria-label="Anotações">
              <span>Anotações</span>
              <button type="button" onClick={() => setAnnotations([])}>
                Limpar {annotations.length}
              </button>
            </div>
          )}
          {annotationMode !== "select" && (
            <span className="pdf-annotation-hint">
              {annotationMode === "area" && "Arraste no PDF para marcar uma área."}
              {annotationMode === "note" && "Clique no PDF para criar uma nota."}
              {annotationMode === "draw" && "Desenhe sobre a página."}
              {annotationMode === "shape" && "Arraste para criar uma forma."}
            </span>
          )}
          {activeResult && (
            <button type="button" className="pdf-search-excerpt" onClick={() => goToPage(activeResult.page)}>
              p. {activeResult.page}: {activeResult.excerpt}
            </button>
          )}
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
              <span>Texto da pagina {pageNumber}</span>
              <button type="button" onClick={() => setTextSize((value) => Math.max(14, value - 1))}>A-</button>
              <button type="button" onClick={() => setTextSize((value) => Math.min(28, value + 1))}>A+</button>
              <button type="button" onClick={() => setLineHeight((value) => Math.max(1.3, Number((value - 0.1).toFixed(1))))}>Espaco -</button>
              <button type="button" onClick={() => setLineHeight((value) => Math.min(2.2, Number((value + 0.1).toFixed(1))))}>Espaco +</button>
            </div>
            <article
              className="pdf-text-page"
              style={{ fontSize: `${textSize}px`, lineHeight }}
            >
              {textLoading ? "Carregando texto..." : highlightText(pageText, searchQuery)}
            </article>
          </section>
        ) : (
          <div className="pdf-pages pdf-highlighter-pages">
            {pdfData ? (
              <PdfLoader
                document={pdfData}
                workerSrc={pdfWorkerUrl}
                beforeLoad={() => <p className="pdf-message">Carregando PDF...</p>}
                errorMessage={() => <p className="pdf-message pdf-message-error">Erro ao carregar PDF</p>}
                onError={(error) => console.error("ERRO AO CARREGAR PDF:", error)}
              >
                {(loadedPdfDocument) => (
                  <PdfHighlighterContent
                    annotationMode={annotationMode}
                    annotations={annotations}
                    drawingStrokeWidth={3}
                    editHighlight={editHighlight}
                    handleDrawingComplete={handleDrawingComplete}
                    handleFreetextClick={handleFreetextClick}
                    handleSelection={handleSelection}
                    handleShapeComplete={handleShapeComplete}
                    highlighterUtilsRef={highlighterUtilsRef}
                    loadedPdfDocument={loadedPdfDocument}
                    pdfScaleValue={pdfScaleValue}
                    removeHighlight={removeHighlight}
                    safeViewMode={safeViewMode}
                    setAnnotationMode={setAnnotationMode}
                    setNumPages={setNumPages}
                    setPdfDocument={setPdfDocument}
                    shapeType={shapeType}
                    visualMode={visualMode}
                  />
                )}
              </PdfLoader>
            ) : (
              <p className="pdf-message">Carregando PDF...</p>
            )}
          </div>
        )}
      </div>
    </div>
  );
}

export default PdfViewer;
