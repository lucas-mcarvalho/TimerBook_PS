import axios from 'axios';
import { buildApiUrl } from '../apiConfig.js';

export const passwordRecoveryService = {
  // Envia o e-mail para pedir o link de recuperação
  requestRecovery: async (email) => {
    const response = await axios.post(buildApiUrl('/forgot/request'), { email });
    return response.data;
  },

  // Valida se o token que está na URL é válido
  validateToken: async (token) => {
    const response = await axios.get(buildApiUrl('/forgot/validate-token'), { 
      params: { token } 
    });
    return response.data;
  },

  // Envia a nova senha junto com o token
  resetPassword: async (token, newPassword) => {
    const response = await axios.post(buildApiUrl('/forgot/reset-password'), { 
      token, 
      newPassword 
    });
    return response.data;
  }
};
