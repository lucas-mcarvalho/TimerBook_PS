import api from "../axiosApi";

export async function getReadingStatsByReadingId(readingId) {
    console.log("Fetching reading stats for readingId:", readingId);    
    try {
        const response = await api.get(`/stats/reading/${readingId}`);
        return response.data;
    } catch (error) {
        throw new Error("Erro ao buscar estatísticas de leitura");
    }
}

export async function getReadingStreakByReadingId(readingId) {
    try {
        const response = await api.get(`/stats/reading/${readingId}/streak`);
        return response.data;
    } catch (error) {
        throw new Error("Erro ao buscar streak de leitura");
    }
}

export async function getGeneralStats(){
    try {
        const response = await api.get("/stats/user/general");
        return response.data;
    } catch (error) {
        throw new Error("Erro ao buscar estatísticas gerais");
    }
}
