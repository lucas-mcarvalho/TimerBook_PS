import { useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';

export default function OAuth2RedirectHandler() {
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    const urlParams = new URLSearchParams(location.search);
    const accessToken = urlParams.get('accessToken');
    const refreshToken = urlParams.get('refreshToken');

    if (accessToken && refreshToken) {
      localStorage.setItem('token', accessToken);
      localStorage.setItem('refreshToken', refreshToken);

      navigate('/home', { replace: true });
    } else {
      navigate('/login?error=falha-oauth2');
    }
  }, [location, navigate]);

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', color: 'white' }}>
      <h2>Autenticando com o Google...</h2>
    </div>
  );
}