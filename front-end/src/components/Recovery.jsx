import React, { useState } from 'react';
import { passwordRecoveryService } from '../features/auth/passwordRecovery';

export default function Recovery() {
  const [email, setEmail] = useState('');
  const [mensagemSucesso, setMensagemSucesso] = useState('');
  const [mensagemErro, setMensagemErro] = useState('');
  const [carregando, setCarregando] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault(); // Evita recarregar a página
    setMensagemSucesso('');
    setMensagemErro('');
    setCarregando(true);

    try {
      const response = await passwordRecoveryService.requestRecovery(email);
      setMensagemSucesso(`Sucesso: ${response.message || 'E-mail enviado!'}`);
      setEmail(''); // Limpa o campo após o envio
    } catch (error) {
      // Tenta pegar a mensagem de erro que veio do Spring Boot, ou exibe uma genérica
      const erroBackend = error.response?.data?.message || 'Erro de conexão. Verifique se o servidor está rodando.';
      setMensagemErro(`Erro: ${erroBackend}`);
    } finally {
      setCarregando(false);
    }
  };

  return (
    <div style={{ maxWidth: '400px', margin: '50px auto', fontFamily: 'sans-serif', padding: '20px', border: '1px solid #ddd', borderRadius: '8px', boxShadow: '0 4px 6px rgba(0,0,0,0.1)' }}>
      <h2 style={{ textAlign: 'center', marginBottom: '20px' }}>Recuperar Senha</h2>
      
      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
        <div>
          <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Seu E-mail:</label>
          <input 
            type="email" 
            placeholder="Digite o e-mail cadastrado" 
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            style={{ width: '100%', padding: '10px', borderRadius: '4px', border: '1px solid #ccc', boxSizing: 'border-box' }}
          />
        </div>

        <button 
          type="submit" 
          disabled={carregando || !email}
          style={{ 
            padding: '12px', 
            backgroundColor: carregando ? '#ccc' : '#007bff', 
            color: 'white', 
            border: 'none', 
            borderRadius: '4px', 
            cursor: carregando ? 'not-allowed' : 'pointer',
            fontWeight: 'bold'
          }}
        >
          {carregando ? 'Enviando aguarde...' : 'Enviar Link de Recuperação'}
        </button>
      </form>

      {/* ÁREA DE FEEDBACK VISUAL NA TELA */}
      <div style={{ marginTop: '20px' }}>
        {mensagemSucesso && (
          <div style={{ padding: '15px', backgroundColor: '#d4edda', color: '#155724', borderRadius: '4px', border: '1px solid #c3e6cb' }}>
            {mensagemSucesso}
          </div>
        )}

        {mensagemErro && (
          <div style={{ padding: '15px', backgroundColor: '#f8d7da', color: '#721c24', borderRadius: '4px', border: '1px solid #f5c6cb' }}>
            {mensagemErro}
          </div>
        )}
      </div>
    </div>
  );
}