import api from "../axiosApi";

export const getUser = async () => {
    try{
        const userData = await api.get("/user/me");
        
        return userData;
    }catch(error){
        console.error("Erro ao obter dados do usuário:", error);
        throw error;
    }
    
}

export const updateProfile = async (userId, userData, photoFile, removePhotoFlag) => {
    const formData = new FormData();
    formData.append("username", userData.username);
    formData.append("email", userData.email);

    if (photoFile) {
        formData.append("photo", photoFile);
    }

    if (removePhotoFlag) {
        formData.append("removePhoto", "true");
    }

    try {
        const response = await api.put(`/user/${userId}`, formData);
        return response.data;
    } catch (error) {
        console.error("Erro ao atualizar perfil:", error);
        throw error;
    }
};

export async function updateReadingGoal(goalMinutes) {
    try {
        const response = await api.put(
            "/user/me/reading-goal",
            {
                dailyReadingGoalMinutes: goalMinutes
            }
        );

        return response.data;

    } catch (error) {
        console.error("Erro ao atualizar meta de leitura:", error.response?.data || error.message);
        throw error;
    }
}

export async function getReadingGoal() {
    try {
        const response = await api.get("/user/me/reading-goal");
        return response.data;
    } catch (error) {
        console.error("Erro ao buscar meta de leitura:", error.response?.data || error.message);
        throw error;
    }
}