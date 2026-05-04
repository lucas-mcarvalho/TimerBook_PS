import { useEffect, useState } from 'react';
import { useLocation, useNavigate, Link } from 'react-router-dom';
import api from "../features/axiosApi";

export default function VerifyEmail() {
  const [status, setStatus] = useState('verificando');
  const location = useLocation();
  const navigate = useNavigate();

  useEffect(() => {
    const verifyToken = async () => {
      const urlParams = new URLSearchParams(location.search);
      const token = urlParams.get('token');

      if (!token) {
        setStatus('erro');
        return;
      }

      try {
        await api.get(`/auth/verify-email?token=${token}`);
        setStatus('sucesso');
        
        setTimeout(() => {
          navigate('/');
        }, 3000);

      } catch (error) {
        setStatus('erro');
      }
    };

    verifyToken();
  }, [location, navigate]);

  return (
    <div style={{ textAlign: 'center', marginTop: '100px', color: 'white' }}>
      {status === 'verificando' && <h2>Verificando seu e-mail, aguarde...</h2>}
      {status === 'sucesso' && <h2 style={{ color: '#2ecc71' }}>E-mail ativado! Redirecionando...</h2>}
      {status === 'erro' && (
        <>
          <h2 style={{ color: '#e74c3c' }}>O link é inválido ou expirou.</h2>
          <Link to="/login" style={{ color: '#3498db' }}>Voltar para o Login</Link>
        </>
      )}
    </div>
  );
}