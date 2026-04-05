export async function getReadingStatsByReadingId(readingId) {
    const response = await fetch(`http://localhost:8080/stats/reading/${readingId}`);

    if (!response.ok) {
        throw new Error("Erro ao buscar estatísticas de leitura");
    }

    return response.json();
}

export async function getReadingStreakByReadingId(readingId) {
    const response = await fetch(`http://localhost:8080/stats/reading/${readingId}/streak`);

    if (!response.ok) {
        throw new Error("Erro ao buscar streak de leitura");
    }

    return response.json();
}