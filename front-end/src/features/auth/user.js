import api from "../axiosApi";

export async function registerUser(username, email, password, photo) {
  const formData = new FormData();

  formData.append("username", username);
  formData.append("email", email);
  formData.append("password", password);

  if (photo) {
    formData.append("photo", photo);
  }

  try {

    const response = await api.post("/auth/register", formData);
    return response.data;
  }catch (error) {
    console.error(
      "Erro ao cadastrar usuário:",
      error.response?.data || error.message
    );
    throw error;
  }
}

export async function loginUser(email, password) {
  

  try {
    const response = await api.post("/auth/login", {
        email,
        password,
   });

    // salva tokens
    localStorage.setItem("token", response.data.token);
    localStorage.setItem("refreshToken", response.data.refreshToken);
    console.log("Login bem-sucedido:", response.data);

    return response.data;
  } catch (error) {
    console.error(
      "Erro ao fazer login:",
      error.response?.data || error.message
    );
    throw error;
  }
}