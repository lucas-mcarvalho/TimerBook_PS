import axios from 'axios';

// Ajuste a URL base se o seu backend estiver rodando em outra porta
const API_URL = 'http://localhost:8080/forgot'; 

export const passwordRecoveryService = {
  // Envia o e-mail para pedir o link de recuperação
  requestRecovery: async (email) => {
    const response = await axios.post(`${API_URL}/request`, { email });
    return response.data;
  },

  // Valida se o token que está na URL é válido
  validateToken: async (token) => {
    const response = await axios.get(`${API_URL}/validate-token`, { 
      params: { token } 
    });
    return response.data;
  },

  // Envia a nova senha junto com o token
  resetPassword: async (token, newPassword) => {
    const response = await axios.post(`${API_URL}/reset-password`, { 
      token, 
      newPassword 
    });
    return response.data;
  }
};