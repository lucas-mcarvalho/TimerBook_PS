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

export const deleteUser = async (userId) => {
    try {
        const response = await api.delete(`/user/${userId}`);
        return response.data;
    } catch (error) {
        console.error("Erro ao deletar usuário:", error);
        throw error;
    }
};
