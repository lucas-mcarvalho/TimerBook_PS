import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8080"
});

// 🔹 REQUEST → adiciona access token (exceto rotas de auth)
api.interceptors.request.use((config) => {
  // ignora rotas de autenticação
  if (config.url.startsWith("/auth")) {
    return config;
  }

  const token = localStorage.getItem("token");

  if (token) {
    config.headers = config.headers || {};
    config.headers.Authorization = `Bearer ${token}`;
  }

  //console.log("Requisição:", token);

  return config;
});

// 🔹 Função de refresh
const refreshAccessToken = async () => {
  const refreshToken = localStorage.getItem("refreshToken");

  const response = await axios.post(
    "http://localhost:8080/auth/refresh",
    { refreshToken },
    { withCredentials: true }
  );

  const { accessToken } = response.data;

  localStorage.setItem("token", accessToken);

  return accessToken;
};

// 🔹 RESPONSE → trata 401 e tenta refresh
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    // segurança extra
    if (!originalRequest || !originalRequest.url) {
      return Promise.reject(error);
    }

    // ignora rotas de auth (login, register, refresh)
    if (originalRequest.url.startsWith("/auth")) {
      return Promise.reject(error);
    }

    if (
      error.response?.status === 401 &&
      !originalRequest._retry
    ) {
      originalRequest._retry = true;

      try {
        const newToken = await refreshAccessToken();

        // atualiza header
        originalRequest.headers.Authorization = `Bearer ${newToken}`;

        // repete request
        return api(originalRequest);
      } catch (refreshError) {
        // logout
        localStorage.removeItem("token");
        localStorage.removeItem("refreshToken");
        localStorage.removeItem("user");

        window.location.href = "/";

        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

export default api;