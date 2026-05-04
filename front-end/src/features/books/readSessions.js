import api from "../axiosApi";

const DEFAULT_START_PAGE = 1;

function normalizePage(page, fallback = DEFAULT_START_PAGE) {
    const numericPage = Number(page);
    return Number.isFinite(numericPage) && numericPage > 0 ? numericPage : fallback;
}

function sortSessionsByStartDesc(sessions) {
    return [...(Array.isArray(sessions) ? sessions : [])].sort(
        (a, b) => new Date(b.startedAt || 0) - new Date(a.startedAt || 0)
    );
}

function getBookIdFromReading(reading) {
    return reading?.book?.id ?? reading?.bookId ?? reading?.book_id;
}

export function findReadingByBookId(readings, bookId) {
    return (Array.isArray(readings) ? readings : []).find(
        (reading) => Number(getBookIdFromReading(reading)) === Number(bookId)
    );
}

export async function startReading(userId, bookId, startPage) {
    const initialPage = normalizePage(startPage);
    try {
        const response = await api.post(`/readings/${userId}/start`, { bookId, startPage: initialPage });
        return response.data;
    } catch (error) {
        console.error("Erro ao iniciar leitura:", error);
        throw new Error("Erro ao iniciar leitura");
    }
}

export async function endReading(readingId, userId, data) {
    try {
        const response = await api.put(
            `/readings/${userId}/${readingId}/finish`,
            data 
        );

        alert("Leitura finalizada com sucesso!");
        console.log("Resposta do servidor ao finalizar leitura:", response.data);
        return response.data;

    } catch (error) {
        console.error("Erro ao finalizar leitura:", error.response?.data || error.message);
        throw error;
    }
}

/*** 
export async function startReadingSession(bookId, startPage){
    const response = await fetch("http://localhost:8080/reading-sessions/start", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ bookId, startPage })
    });

    if (!response.ok) {
        throw new Error("Erro ao iniciar sessão de leitura");
    }

    return response.json();
} 
***/

export async function endReadingSession(sessionId, endPage){
    try {
        const response = await api.put(`/reading-sessions/${sessionId}/finish`, { endPage });
        return response.data;
    } catch (error) {
        console.error("Erro ao finalizar sessão de leitura:", error);
        throw new Error("Erro ao finalizar sessão de leitura");
    }
}

export async function getReadingSessions() {
    try {
        const response = await api.get("/reading-sessions");
        return response.data;
    } catch (error) {
        console.error("Erro ao buscar sessões de leitura:", error);
        throw new Error("Erro ao buscar sessões de leitura");
    }
}


export async function getSessionsByReadingId(readingId) {
    try {
        const response = await api.get(`/reading-sessions/reading/${readingId}`);
        return (Array.isArray(response.data) ? response.data : []).map((session) => ({
            ...session,
            readingId: session.readingId ?? session.reading_id ?? session.reading?.id ?? readingId,
        }));
    } catch (error) {
        console.error("Erro ao buscar sessões por leitura:", error);
        throw new Error("Erro ao buscar sessões por leitura");
    }
}   

export async function getReadingsInProgress() {
  try {
    const response = await api.get("/stats/books-in-progress");
    return Array.isArray(response.data) ? response.data : [];
  } catch (error) {
    console.error("Erro ao buscar leituras em andamento:", error);
    throw error;
  }
}

export async function getReadingInProgressByBookId(bookId) {
    const readings = await getReadingsInProgress();
    return findReadingByBookId(readings, bookId);
}

export async function startBookReadingSession(userId, book) {
    const bookId = typeof book === "object" ? book?.id : book;

    if (!userId || !bookId) {
        throw new Error("Usuário ou livro não informado.");
    }

    let startPage = DEFAULT_START_PAGE;

    try {
        const activeReading = await getReadingInProgressByBookId(bookId);

        if (activeReading?.id) {
            const previousSessions = await getSessionsByReadingId(activeReading.id);
            const latestSessionWithPage = sortSessionsByStartDesc(previousSessions).find(
                (session) => session.endPage !== null && session.endPage !== undefined
            );

            startPage = normalizePage(
                latestSessionWithPage?.endPage ?? activeReading.currentPage,
                DEFAULT_START_PAGE
            );
        }
    } catch (error) {
        console.warn("Não foi possível recuperar progresso anterior. Iniciando da página padrão.", error);
    }

    const reading = await startReading(userId, bookId, startPage);
    const readingId = reading?.id;

    if (!readingId) {
        throw new Error("Não foi possível identificar a leitura iniciada.");
    }

    const sessions = await getSessionsByReadingId(readingId);
    const sortedSessions = sortSessionsByStartDesc(sessions);
    const currentSession = sortedSessions.find((session) => !session.endedAt) ?? sortedSessions[0];

    if (!currentSession?.id) {
        throw new Error("Sessão de leitura não encontrada após iniciar a leitura.");
    }

    return {
        reading,
        readingId,
        sessionId: currentSession.id,
        initialPage: startPage,
        sessions,
    };
}
