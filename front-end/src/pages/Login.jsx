import { useState, useEffect } from "react";
import { loginUser } from "../features/auth/user.js";
import { useNavigate } from "react-router-dom";

import TimerBookLogo from '../assets/Home/TimerbookLogo.svg'; 
import MoonIcon from '../assets/Home/MoonIcon.svg';
import SunIcon from '../assets/Home/SunIcon.svg';
import "../styles/Login.css";
import "../styles/LoginLight.css"; 
import '../styles/HomeDark.css'; 

export default function Login() {
  const navigate = useNavigate();

  const [isDarkMode, setIsDarkMode] = useState(() => {
    const savedTheme = localStorage.getItem('timerbook-theme');
    return savedTheme === 'dark';
  });

  useEffect(() => {
    localStorage.setItem('timerbook-theme', isDarkMode ? 'dark' : 'light');
  }, [isDarkMode]);

  const [formData, setFormData] = useState({
    email: "",
    password: "",
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);
  
  const [isFocused, setIsFocused] = useState(false);
  const [bookSpeed, setBookSpeed] = useState('0.8s'); 

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleFocus = () => {
    setIsFocused(true);
    setBookSpeed('0.8s');
    setTimeout(() => {
      setBookSpeed('0.15s');
    }, 800);
  };

  const handleBlur = () => {
    setIsFocused(false);
    setBookSpeed('0.8s');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (loading) return;
    setLoading(true);
    setError(null);
    setSuccess(false);

    try {
      await loginUser(formData.email, formData.password);
      setSuccess(true);
      navigate('/');
    } catch (err) {
      setError(err instanceof Error ? err.message : "Erro ao fazer login");
    } finally {
      setLoading(false);
    }
  };

  const charCount = formData.email.length + formData.password.length;
  const pageAngle = isFocused ? Math.max(-175, charCount * -10) : 0;
  const leftLinesCount = Math.min(20, charCount);
  const rightLinesCount = Math.min(20, Math.max(0, charCount - 20));

  return (
    <div className={`login-container ${isDarkMode ? 'dark-theme' : 'light-theme'}`}>
      
      <div className="form-block">
        <div className="login-form-card">
          <div className="logo-block">
            <img src={TimerBookLogo} alt="Logo Pequena" />
            <h1>TimerBook</h1>
          </div>
          
          <p className="welcome-text">Acesse sua biblioteca pessoal</p>

          {success && <div className="status-message status-success">✓ Login realizado com sucesso!</div>}
          {error && <div className="status-message status-error">✗ {error}</div>}

          <form className="profile-form">
            <div className="form-group">
              <label>Email</label>
              <input
                type="email"
                name="email"
                value={formData.email}
                onChange={handleInputChange}
                onFocus={handleFocus}
                onBlur={handleBlur}
                required
                placeholder="seu.email@exemplo.com"
              />
            </div>

            <div className="form-group" style={{ marginTop: '20px' }}>
              <label>Senha</label>
              <input
                type="password"
                name="password"
                value={formData.password}
                onChange={handleInputChange}
                onFocus={handleFocus}
                onBlur={handleBlur}
                required
                placeholder="Sua senha"
              />
            </div>

            <button 
              type="submit"
              onClick={handleSubmit} 
              className={`btn-concluir ${loading ? 'disabled' : ''}`}
              disabled={loading}
              style={{ width: '100%', marginTop: '30px' }}
            >
              {loading ? "Entrando..." : "Entrar"}
            </button>

            <div className="form-footer-links">
              <a href="/esqueceu-senha" className="welcome-text">Esqueci minha senha</a>
              <a href="/cadastrar-usuario" style={{ color: '#2ecc71' }}>Criar conta</a>
            </div>
          </form>
        </div>
      </div>

      <div className="animation-block">
        <div className={`book-scene ${isFocused ? 'is-open' : ''}`}>
          <div className="book-cover-back"></div>
          <div className="book-page-right">
            <div className="page-content">
              {Array.from({ length: rightLinesCount }).map((_, index) => (
                <div key={`right-${index}`} className="fake-line"></div>
              ))}
            </div>
          </div>
          <div 
            className="book-page-moving" 
            style={{ 
              transform: `rotateY(${pageAngle}deg)`,
              transition: `transform ${bookSpeed} ${bookSpeed === '0.8s' ? 'cubic-bezier(0.25, 1, 0.5, 1)' : 'ease-out'}`
            }} 
          >
            <div className="page-content">
              {Array.from({ length: leftLinesCount }).map((_, index) => (
                <div key={`left-${index}`} className="fake-line"></div>
              ))}
            </div>
          </div>
          <div className="book-cover-front">
              <img src={TimerBookLogo} alt="TimerBook Logo" className="cover-logo" />
          </div>
        </div>
      </div>

      <div className="login-theme-toggle">
        <button className="action-icon-btn" onClick={() => setIsDarkMode(!isDarkMode)}>
          <img src={isDarkMode ? SunIcon : MoonIcon} alt="Alternar Aparência" className="nav-icon" />
        </button>
      </div>

    </div>
  );
}