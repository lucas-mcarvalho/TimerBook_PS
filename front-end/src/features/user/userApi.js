import api from "../axiosApi";

export const getUser = async () => {
    try{
        const userData = await api.get("/user/me");
        console.log("Dados do usuário obtidos:", userData);
        return userData;
    }catch(error){
        console.error("Erro ao obter dados do usuário:", error);
        throw error;
    }
    
}