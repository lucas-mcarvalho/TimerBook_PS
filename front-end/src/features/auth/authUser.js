import api from "../axiosApi";

export async function registerUser(username, email, password, photo) {
  const formData = new FormData();

  formData.append("username", username);
  formData.append("email", email);
  formData.append("password", password);
  
  if(photo) {
    formData.append("photo", photo);
  }

  try{
    const response = await api.post("/auth/register", formData);
    return response.data;
  }catch(error) {
    console.error("Erro ao cadastrar usuário:", error.response?.data || error.message);
    throw error;
  }
}

export async function loginUser(email, password) {
  const formData = new FormData();
  formData.append("email", email);
  formData.append("password", password);

  try {
    const response = await api.post("/auth/login", formData);

    const { accessToken, refreshToken } = response.data;

    // salva tokens
    localStorage.setItem("token", accessToken);
    localStorage.setItem("refreshToken", refreshToken);

    return response.data;
  } catch (error) {
    console.error(
      "Erro ao fazer login:",
      error.response?.data || error.message
    );
    throw error;
  }
}