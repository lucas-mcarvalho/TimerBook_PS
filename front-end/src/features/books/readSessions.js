export async function startReading(bookId) {
    const response = await fetch("http://localhost:8080/readings/start", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ bookId })
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
