import { useState, useEffect, useRef, useCallback, useMemo } from "react";
import { Document, Page, pdfjs } from "react-pdf";
import "../styles/PdfViewer.css";
import "../styles/TextLayer.css";
import "../styles/AnnotationLayer.css";

pdfjs.GlobalWorkerOptions.workerSrc = "/pdf.worker.min.js";

const SPEECH_RATES = [0.5, 1, 1.5, 2];
const DEFAULT_SPEECH_RATE = 1;

const DEFAULT_PREFERENCES = {
  viewMode: "continuous",
  zoom: 1,
  fitWidth: true,
  visualMode: "normal",
  textMode: false,
  textSize: 18,
  lineHeight: 1.7,
  rotation: 0,
  speechRate: DEFAULT_SPEECH_RATE,
  speechVoiceURI: "",
};

const VIEW_MODES = new Set(["continuous", "single"]);

// ===========================================================================
// Fallback de tradução direta no front caso o endpoint Java não seja informado.
// ===========================================================================

async function fetchFreeTranslation(text) {
  const url = `https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=pt&dt=t&q=${encodeURIComponent(text)}`;
  const response = await fetch(url);
  if (!response.ok) throw new Error("Erro na rede do tradutor gratuito");
  const data = await response.json();
  
  return data[0].map((pedaco) => pedaco[0]).join("");
}

// ===========================================================================
// UTILITÁRIOS
// ===========================================================================

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

function normalizeSpeechRate(value) {
  const numericValue = Number(value);
  return SPEECH_RATES.includes(numericValue) ? numericValue : DEFAULT_SPEECH_RATE;
}

function escapeRegExp(value) {
  return value.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
}

function highlightText(text, query) {
  if (!query.trim()) return text;

  const parts = text.split(new RegExp(`(${escapeRegExp(query.trim())})`, "gi"));
  return parts.map((part, index) => (
    part.toLowerCase() === query.trim().toLowerCase()
      ? <mark key={`highlight-${index}-${part}`}>{part}</mark>
      : part
  ));
}

function prepareTextForSpeech(text) {
  return text
    .replace(/\r/g, "\n")
    .replace(/([A-Za-zÀ-ÿ])-\s*\n\s*([A-Za-zÀ-ÿ])/g, "$1$2")
    .replace(/[ \t]*\n[ \t]*/g, " ")
    .replace(/\s{2,}/g, " ")
    .trim();
}

function splitTextForSpeech(text, maxLength = 220) {
  const sentences = text.match(/[^.!?;:]+[.!?;:]?/g) || [text];
  const chunks = [];
  let currentChunk = "";

  const pushLongPart = (part) => {
    let wordChunk = "";

    part.split(/\s+/).forEach((word) => {
      if (!word) return;

      const nextWordChunk = wordChunk ? `${wordChunk} ${word}` : word;
      if (nextWordChunk.length > maxLength && wordChunk) {
        chunks.push(wordChunk);
        wordChunk = word;
      } else {
        wordChunk = nextWordChunk;
      }
    });

    if (wordChunk) chunks.push(wordChunk);
  };

  sentences.forEach((rawSentence) => {
    const sentence = rawSentence.trim();
    if (!sentence) return;

    if (sentence.length > maxLength) {
      sentence.split(/,\s+|\s+-\s+/).forEach((part) => {
        const cleanedPart = part.trim();
        if (cleanedPart) pushLongPart(cleanedPart);
      });
      return;
    }

    const nextChunk = currentChunk ? `${currentChunk} ${sentence}` : sentence;
    if (nextChunk.length > maxLength && currentChunk) {
      chunks.push(currentChunk);
      currentChunk = sentence;
    } else {
      currentChunk = nextChunk;
    }
  });

  if (currentChunk) chunks.push(currentChunk);
  return chunks;
}

function getPortugueseVoiceScore(voice) {
  const name = voice.name.toLowerCase();
  const lang = voice.lang.toLowerCase();
  let score = 0;

  if (lang === "pt-br") score += 120;
  else if (lang.startsWith("pt")) score += 80;
  else score -= 100;

  if (name.includes("natural")) score += 35;
  if (name.includes("neural")) score += 35;
  if (name.includes("online")) score += 25;
  if (name.includes("google")) score += 25;
  if (name.includes("microsoft")) score += 15;
  if (name.includes("brasil") || name.includes("brazil")) score += 15;
  if (name.includes("portugu")) score += 10;

  return score;
}

function chooseBestPortugueseVoice(voices) {
  return [...voices]
    .filter((voice) => voice.lang.toLowerCase().startsWith("pt"))
    .sort((voiceA, voiceB) => getPortugueseVoiceScore(voiceB) - getPortugueseVoiceScore(voiceA))[0] || null;
}

// ---------------------------------------------------------------------------
// COMPONENTE PRINCIPAL
// ---------------------------------------------------------------------------

function PdfViewer({
  file,
  initialPage = 1,
  onPageChange,
  storageKey = "default",
  onSearchRequest,
  onTextPageRequest,
  onTranslatePageRequest,
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
  const [speechRate, setSpeechRate] = useState(() => normalizeSpeechRate(savedPreferences.speechRate));
  const [speechVoiceURI, setSpeechVoiceURI] = useState(savedPreferences.speechVoiceURI || DEFAULT_PREFERENCES.speechVoiceURI);
  const [bookmarks, setBookmarks] = useState(savedBookmarks);
  const [containerWidth, setContainerWidth] = useState(700);
  const [pageText, setPageText] = useState("");
  
  // =====================================
  // ESTADOS DE TRADUÇÃO
  // =====================================
  const [translatedText, setTranslatedText] = useState("");
  const [translationCache, setTranslationCache] = useState({});
  const [translationLoading, setTranslationLoading] = useState(false);
  const [isTranslationActive, setIsTranslationActive] = useState(false);
  const [translationError, setTranslationError] = useState("");
  
  const [textLoading, setTextLoading] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");
  const [searchResults, setSearchResults] = useState([]);
  const [activeSearchIndex, setActiveSearchIndex] = useState(-1);
  const [searching, setSearching] = useState(false);
  
  // =====================================
  // ESTADOS E REFS DO ÁUDIO
  // =====================================
  const [isSpeaking, setIsSpeaking] = useState(false);
  const [isSpeechPaused, setIsSpeechPaused] = useState(false);
  const [speechSupported, setSpeechSupported] = useState(false);
  const [availableVoices, setAvailableVoices] = useState([]);
  const speechSessionRef = useRef(0);

  const containerRef = useRef(null);
  const viewportRef = useRef(null);
  const pageRefs = useRef([]);
  const observerRef = useRef(null);
  const searchInputRef = useRef(null);
  const isScrollingToPage = useRef(false);
  const hasScrolledToInitial = useRef(false);

  const onPageChangeRef = useRef(onPageChange);
  useEffect(() => { onPageChangeRef.current = onPageChange; }, [onPageChange]);

  useEffect(() => {
    onPageChangeRef.current?.(pageNumber);
  }, [pageNumber]);

  useEffect(() => {
    const preferences = {
      viewMode, zoom, fitWidth, visualMode, textMode, textSize, lineHeight, rotation, speechRate, speechVoiceURI,
    };
    localStorage.setItem(preferencesKey, JSON.stringify(preferences));
  }, [fitWidth, lineHeight, preferencesKey, rotation, speechRate, speechVoiceURI, textMode, textSize, viewMode, visualMode, zoom]);

  useEffect(() => {
    localStorage.setItem(bookmarksKey, JSON.stringify(bookmarks));
  }, [bookmarks, bookmarksKey]);

  useEffect(() => {
    if (!("speechSynthesis" in window) || !("SpeechSynthesisUtterance" in window)) {
      setSpeechSupported(false);
      return undefined;
    }

    let retryCount = 0;
    let retryTimer;

    const loadVoices = () => {
      setSpeechSupported(true);
      const voices = window.speechSynthesis.getVoices();
      setAvailableVoices(voices);

      if (!voices.length && retryCount < 8) {
        retryCount += 1;
        retryTimer = window.setTimeout(loadVoices, 250);
      }
    };

    loadVoices();
    window.speechSynthesis.addEventListener("voiceschanged", loadVoices);

    return () => {
      window.clearTimeout(retryTimer);
      window.speechSynthesis.removeEventListener("voiceschanged", loadVoices);
      window.speechSynthesis.cancel();
      speechSessionRef.current += 1;
    };
  }, []);

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

    let timeoutId;
    const resizeObserver = new ResizeObserver(([entry]) => {
      clearTimeout(timeoutId);
      timeoutId = setTimeout(() => {
        setContainerWidth(entry.contentRect.width);
      }, 100);
    });

    resizeObserver.observe(target);
    return () => {
      resizeObserver.disconnect();
      clearTimeout(timeoutId);
    };
  }, []);

  // ---------------------------------------------------------------------------
  // Extração do Texto
  // ---------------------------------------------------------------------------
  useEffect(() => {
    let cancelled = false;

    const loadPageText = async () => {
      if (!textMode || !onTextPageRequest) {
        setPageText("");
        setTranslatedText("");
        setIsTranslationActive(false);
        setTranslationError("");
        return;
      }

      setTextLoading(true);
      try {
        const text = await onTextPageRequest(pageNumber);
        if (!cancelled) {
          setPageText(text || "Nenhum texto extraível nesta página.");
          setTranslatedText("");
          setIsTranslationActive(false);
          setTranslationError("");
        }
      } catch (error) {
        console.error("Erro ao obter texto da página:", error);
        if (!cancelled) setPageText("Não foi possível obter o texto desta página.");
      } finally {
        if (!cancelled) setTextLoading(false);
      }
    };

    loadPageText();
    return () => { cancelled = true; };
  }, [textMode, pageNumber, onTextPageRequest]);

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

  const handleSearch = async () => {
    const query = searchQuery.trim();
    if (!query || !onSearchRequest) {
      setSearchResults([]);
      setActiveSearchIndex(-1);
      return;
    }

    setSearching(true);
    try {
      const results = await onSearchRequest(query);
      setSearchResults(results);
      setActiveSearchIndex(results.length ? 0 : -1);
      if (results.length) goToPage(results[0].page);
    } catch (error) {
      console.error("Erro ao buscar no PDF:", error);
      setSearchResults([]);
      setActiveSearchIndex(-1);
    } finally {
      setSearching(false);
    }
  };

  // =====================================
  // APLICAÇÃO DA TRADUÇÃO
  // =====================================
  const handleTranslatePageText = async () => {
    if (!pageText.trim()) return;

    if (translationCache[pageNumber]) {
      setTranslatedText(translationCache[pageNumber]);
      setIsTranslationActive(true);
      return;
    }

    setTranslationLoading(true);
    setTranslationError("");
    
    const currentPageAtRequest = pageNumber;

    try {
      const translated = onTranslatePageRequest
        ? await onTranslatePageRequest(currentPageAtRequest, pageText)
        : await fetchFreeTranslation(pageText);
      
      if (translated) {
        setTranslationCache((prev) => ({ ...prev, [currentPageAtRequest]: translated }));
      }

      if (currentPageAtRequest === pageNumber) {
        setTranslatedText(translated || "Não foi possível traduzir o texto desta página.");
        setIsTranslationActive(true);
      }
    } catch (error) {
      console.error("Erro no fluxo de tradução:", error);
      if (currentPageAtRequest === pageNumber) {
        setTranslationError("Erro na tradução. Tente novamente.");
        setIsTranslationActive(false);
        setTranslatedText("");
      }
    } finally {
      if (currentPageAtRequest === pageNumber) {
        setTranslationLoading(false);
      }
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

  const stopSpeaking = useCallback(() => {
    if ("speechSynthesis" in window) {
      window.speechSynthesis.cancel();
    }
    speechSessionRef.current += 1;
    setIsSpeaking(false);
    setIsSpeechPaused(false);
  }, []);

  const pauseSpeaking = useCallback(() => {
    if (!isSpeaking || isSpeechPaused || !("speechSynthesis" in window)) return;

    window.speechSynthesis.pause();
    setIsSpeechPaused(true);
  }, [isSpeaking, isSpeechPaused]);

  const resumeSpeaking = useCallback(() => {
    if (!isSpeaking || !isSpeechPaused || !("speechSynthesis" in window)) return;

    window.speechSynthesis.resume();
    setIsSpeechPaused(false);
  }, [isSpeaking, isSpeechPaused]);

  const cycleSpeechRate = useCallback(() => {
    setSpeechRate((currentRate) => {
      const normalizedRate = normalizeSpeechRate(currentRate);
      const currentIndex = SPEECH_RATES.indexOf(normalizedRate);
      return SPEECH_RATES[(currentIndex + 1) % SPEECH_RATES.length];
    });
  }, []);

  const speakPage = useCallback(() => {
    const currentText = isTranslationActive ? translatedText : pageText;
    
    if (!speechSupported || !currentText.trim()) return;

    const preparedText = prepareTextForSpeech(currentText);
    const chunks = splitTextForSpeech(preparedText);
    if (!chunks.length) return;

    stopSpeaking();

    const sessionId = speechSessionRef.current + 1;
    const selectedVoice = availableVoices.find((voice) => voice.voiceURI === speechVoiceURI)
      || chooseBestPortugueseVoice(availableVoices);
    let chunkIndex = 0;

    speechSessionRef.current = sessionId;
    setIsSpeaking(true);
    setIsSpeechPaused(false);

    const speakNextChunk = () => {
      if (speechSessionRef.current !== sessionId) return;

      const chunk = chunks[chunkIndex];
      if (!chunk) {
        setIsSpeaking(false);
        setIsSpeechPaused(false);
        return;
      }

      const utterance = new SpeechSynthesisUtterance(chunk);
      utterance.lang = selectedVoice?.lang || "pt-BR";
      utterance.voice = selectedVoice;
      utterance.rate = speechRate;
      utterance.pitch = 1.04;
      utterance.volume = 1;

      utterance.onend = () => {
        if (speechSessionRef.current !== sessionId) return;
        chunkIndex += 1;
        speakNextChunk();
      };

      utterance.onerror = (event) => {
        if (event.error !== "interrupted" && event.error !== "canceled") {
          console.error("Erro na síntese de voz:", event.error);
        }

        if (speechSessionRef.current === sessionId) {
          setIsSpeaking(false);
          setIsSpeechPaused(false);
        }
      };

      window.speechSynthesis.speak(utterance);
    };

    speakNextChunk();
  }, [availableVoices, pageText, translatedText, isTranslationActive, speechRate, speechSupported, speechVoiceURI, stopSpeaking]);

  const baseWidth = fitWidth ? Math.min(900, Math.max(320, containerWidth - 48)) : 650;
  const pageWidth = Math.round(baseWidth * zoom);
  const isCurrentPageBookmarked = bookmarks.includes(pageNumber);
  const activeResult = activeSearchIndex >= 0 ? searchResults[activeSearchIndex] : null;
  const canSpeakPage = speechSupported && !textLoading && Boolean(pageText.trim());
  const portugueseVoices = availableVoices.filter((voice) => voice.lang.toLowerCase().startsWith("pt"));
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
            onChange={(event) => setSearchQuery(event.target.value)}
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

      {(bookmarks.length > 0 || activeResult) && (
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
              {portugueseVoices.length > 1 && (
                <select
                  value={speechVoiceURI}
                  onChange={(event) => setSpeechVoiceURI(event.target.value)}
                  aria-label="Voz da leitura"
                  title="Voz da leitura"
                >
                  <option value="">Voz auto</option>
                  {portugueseVoices.map((voice) => (
                    <option key={voice.voiceURI} value={voice.voiceURI}>
                      {voice.name}
                    </option>
                  ))}
                </select>
              )}
              <button
                type="button"
                className="pdf-speech-rate-button"
                onClick={cycleSpeechRate}
                aria-label={`Velocidade da voz: ${speechRate}x`}
                title="Mudar velocidade da voz"
              >
                {speechRate}x
              </button>
              
              <button
                type="button"
                className={isTranslationActive ? "active" : ""}
                onClick={handleTranslatePageText}
                disabled={!pageText.trim() || translationLoading}
                aria-pressed={isTranslationActive}
              >
                {translationLoading ? "Traduzindo…" : isTranslationActive ? "Atualizar tradução" : "Traduzir PT-BR"}
              </button>
              {isTranslationActive && (
                <button
                  type="button"
                  onClick={() => setIsTranslationActive(false)}
                  disabled={translationLoading}
                >
                  Original
                </button>
              )}
              
              <button
                type="button"
                className={isSpeaking ? "active" : ""}
                onClick={speakPage}
                disabled={!isSpeaking && !canSpeakPage}
                aria-pressed={isSpeaking}
                title={speechSupported ? "Ler esta página em voz alta" : "Leitura em voz não suportada neste navegador"}
              >
                {isSpeaking ? "Reiniciar" : "Ler página"}
              </button>
              {isSpeaking && (
                <>
                  <button
                    type="button"
                    className={isSpeechPaused ? "active" : ""}
                    onClick={isSpeechPaused ? resumeSpeaking : pauseSpeaking}
                    aria-pressed={isSpeechPaused}
                  >
                    {isSpeechPaused ? "Retomar" : "Pausar"}
                  </button>
                  <button type="button" onClick={stopSpeaking}>
                    Parar
                  </button>
                </>
              )}
            </div>
            <article
              className="pdf-text-page"
              style={{ fontSize: `${textSize}px`, lineHeight }}
            >
              {textLoading
                ? "Carregando texto..."
                : translationLoading
                  ? "Traduzindo texto..."
                  : highlightText(isTranslationActive ? translatedText : pageText, searchQuery)}
            </article>
            {translationError && (
              <div className="pdf-text-error" role="alert">
                {translationError}
              </div>
            )}
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
