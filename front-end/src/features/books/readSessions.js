import api from "../axiosApi";

export async function startReading(userId, bookId, startPage) {
    startPage = startPage || 1; // Define página inicial padrão como 1
    try {
        const response = await api.post(`/readings/${userId}/start`, { bookId, startPage });
        return response.data;
    } catch (error) {
        console.error("Erro ao iniciar leitura:", error);
        throw new Error("Erro ao iniciar leitura");
    }
}

export async function endReading(readingId, userId, data) {
    try {
        const response = await api.put(
            `http://localhost:8080/readings/${userId}/${readingId}/finish`,
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
        const response = await api.put(`http://localhost:8080/reading-sessions/${sessionId}/finish`, { endPage });
        return response.data;
    } catch (error) {
        console.error("Erro ao finalizar sessão de leitura:", error);
        throw new Error("Erro ao finalizar sessão de leitura");
    }
}

export async function getReadingSessions() {
    try {
        const response = await api.get("http://localhost:8080/reading-sessions");
        return response.data;
    } catch (error) {
        console.error("Erro ao buscar sessões de leitura:", error);
        throw new Error("Erro ao buscar sessões de leitura");
    }
}


export async function getSessionsByReadingId(readingId) {
    try {
        const response = await api.get(`http://localhost:8080/reading-sessions/reading/${readingId}`);
        return response.data;
    } catch (error) {
        console.error("Erro ao buscar sessões por leitura:", error);
        throw new Error("Erro ao buscar sessões por leitura");
    }
}   

export async function getReading(bookId, userId) {
    try {
        const response = await api.get(`http://localhost:8080/readings/${userId}/${bookId}`);
        console.log("Leitura obtida:", response.data);
        return response.data;
    } catch (error) {
        console.error("Erro ao buscar leitura:", error);
        throw new Error("Erro ao buscar leitura");
    }
}   
