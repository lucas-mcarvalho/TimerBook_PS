export async function startReading(bookId, startPage) {
    startPage = startPage || 1; // Define página inicial padrão como 1
    const response = await fetch("http://localhost:8080/readings/start", {
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

export async function endReading(readingId) {
    const response = await fetch(`http://localhost:8080/readings/${readingId}/finish`, {
        method: "PUT"
    });

    if (!response.ok) {
        throw new Error("Erro ao finalizar sessão de leitura");
    }

    return response.json();
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
    const response = await fetch(`http://localhost:8080/reading-sessions/${sessionId}/finish`, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ endPage })
    });

    if (!response.ok) {
        throw new Error("Erro ao finalizar sessão de leitura");
    }

    return response.json();
}

export async function getReadingSessions() {
    const response = await fetch("http://localhost:8080/reading-sessions");

    if (!response.ok) {
        throw new Error("Erro ao buscar sessões de leitura");
    }

    return response.json();
}


export async function getSessionsByReadingId(readingId) {
    const response = await fetch(`http://localhost:8080/reading-sessions/reading/${readingId}`);

    if (!response.ok) {
        throw new Error("Erro ao buscar sessões de leitura por ID");
    }

    return response.json();
}   
