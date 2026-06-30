import { useState, useEffect } from "react";
import { loginUser } from "../features/auth/user.js";
import { useNavigate } from "react-router-dom";
import { useToast } from "../components/ToastContext.js";

import TimerBookLogo from '../assets/Home/TimerbookLogo.svg'; 
import MoonIcon from '../assets/Home/MoonIcon.svg';
import SunIcon from '../assets/Home/SunIcon.svg';
import "../styles/Login.css";
import "../styles/LoginLight.css"; 
import '../styles/HomeDark.css'; 

export default function Login() {
  const { showAchievementToast } = useToast();
  const navigate = useNavigate();

  const [isDarkMode, setIsDarkMode] = useState(() => {
    const savedTheme = localStorage.getItem('timerbook-theme');
    return savedTheme === 'dark';
  });

  const [mostrarSenha, setMostrarSenha] = useState(false);

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

  const handleGoogleLogin = () => {
    window.location.href = "http://localhost:8080/oauth2/authorization/google";
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (loading) return;
    setLoading(true);
    setError(null);
    setSuccess(false);

    try {
      const response = await loginUser(formData.email, formData.password);
      setSuccess(true);

      const novas = response?.novasConquistas || response?.data?.novasConquistas;
      if (novas && novas.length > 0) {
        novas.forEach(conquista => {
          showAchievementToast(conquista);
        });
      }
      navigate('/home', { replace: true });
    } catch (err) {
      const status = err.response?.status;
      const message = err.response?.data;

      if (status === 400 && typeof message === 'string') {
        const lowerMessage = message.toLowerCase();
        
        if (lowerMessage.includes("usuario nao encontrado")) {
          setError("Usuário não encontrado. Verifique o e-mail digitado.");
        } else if (lowerMessage.includes("bad credentials") || lowerMessage.includes("senha incorreta")) {
          setError("E-mail ou senha incorretos. Tente novamente.");
        } else if (lowerMessage.includes("user is disabled") || lowerMessage.includes("usuário desabilitado")) {
          setError("Sua conta ainda não foi ativada. Verifique seu e-mail.");
        } else {
          setError(message);
        }
      } else {
        setError("Erro ao conectar com o servidor. Tente mais tarde.");
      }
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

            {/* SENHA */}
            <div className="form-group" style={{ marginTop: '20px' }}>
              <label>Senha</label>

              <div style={{ position: "relative" }}>
                <input
                  type={mostrarSenha ? "text" : "password"}
                  name="password"
                  value={formData.password}
                  onChange={handleInputChange}
                  onFocus={handleFocus}
                  onBlur={handleBlur}
                  required
                  placeholder="Sua senha"
                  style={{ width: "100%", paddingRight: "40px" }}
                />

                <button
                  type="button"
                  onClick={() => setMostrarSenha(!mostrarSenha)}
                  style={{
                    position: "absolute",
                    right: "10px",
                    top: "50%",
                    transform: "translateY(-50%)",
                    background: "none",
                    border: "none",
                    cursor: "pointer"
                  }}
                >
                  {mostrarSenha ? (
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke={isDarkMode ? "#fff" : "#333"} strokeWidth="2" style={{ transition: 'stroke 0.4s ease' }}>
                      <path d="M17.94 17.94A10.94 10.94 0 0 1 12 19C7 19 2.73 15.11 1 12C1.68 10.82 2.61 9.73 3.74 8.86M9.9 4.24A10.94 10.94 0 0 1 12 5C17 5 21.27 8.89 23 12C22.35 13.11 21.5 14.14 20.5 15.03M1 1L23 23" />
                    </svg>
                  ) : (
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke={isDarkMode ? "#fff" : "#333"} strokeWidth="2" style={{ transition: 'stroke 0.4s ease' }}>
                      <path d="M1 12C1 12 5 4 12 4C19 4 23 12 23 12C23 12 19 20 12 20C5 20 1 12 1 12Z" />
                      <circle cx="12" cy="12" r="3" />
                    </svg>
                  )}
                </button>
              </div>
            </div>

            {/* BOTÃO ENTRAR NORMAL */}
            <button 
              type="submit"
              onClick={handleSubmit} 
              className={`btn-concluir ${loading ? 'disabled' : ''}`}
              disabled={loading}
              style={{ width: '100%', marginTop: '30px' }}
            >
              {loading ? "Entrando..." : "Entrar"}
            </button>

            <div style={{ display: 'flex', alignItems: 'center', margin: '20px 0' }}>
              <div style={{ flex: 1, height: '1px', backgroundColor: isDarkMode ? '#444' : '#ddd', transition: 'background-color 0.4s ease' }}></div>
              <span style={{ margin: '0 10px', color: isDarkMode ? '#888' : '#666', fontSize: '14px', transition: 'color 0.4s ease' }}>ou</span>
              <div style={{ flex: 1, height: '1px', backgroundColor: isDarkMode ? '#444' : '#ddd', transition: 'background-color 0.4s ease' }}></div>
            </div>

            <button
              type="button"
              onClick={handleGoogleLogin}
              className="btn-google"
            >
              <img 
                src="https://upload.wikimedia.org/wikipedia/commons/c/c1/Google_%22G%22_logo.svg" 
                alt="Google Logo" 
                style={{ width: '20px', height: '20px' }} 
              />
              Continuar com o Google
            </button>

            <div className="form-footer-links" style={{ marginTop: '20px' }}>
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
