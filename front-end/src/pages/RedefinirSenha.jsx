import { useState, useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { passwordRecoveryService } from "../features/auth/passwordRecoveryService.js";

import TimerBookLogo from '../assets/Home/TimerbookLogo.svg'; 
import MoonIcon from '../assets/Home/MoonIcon.svg';
import SunIcon from '../assets/Home/SunIcon.svg';

import "../styles/Login.css";
import "../styles/LoginLight.css"; 
import '../styles/HomeDark.css'; 

export default function RedefinirSenha() {
  const navigate = useNavigate();

  const [searchParams] = useSearchParams();
  const token = searchParams.get("token");

  const [isDarkMode, setIsDarkMode] = useState(() => {
    const savedTheme = localStorage.getItem('timerbook-theme');
    return savedTheme === 'dark';
  });

  useEffect(() => {
    localStorage.setItem('timerbook-theme', isDarkMode ? 'dark' : 'light');
  }, [isDarkMode]);

  const [formData, setFormData] = useState({
    password: "",
    confirmPassword: "",
  });

  const [mostrarSenha, setMostrarSenha] = useState(false);
  const [mostrarConfirm, setMostrarConfirm] = useState(false);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);

  const eyeColor = isDarkMode ? "#e5e5e5" : "#444";

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (loading) return;

    if (!token) {
      setError("Token inválido ou ausente na URL.");
      return;
    }

    if (formData.password !== formData.confirmPassword) {
      setError("As senhas não coincidem.");
      return;
    }

    setLoading(true);
    setError(null);
    setSuccess(false);

    try {
      await passwordRecoveryService.resetPassword(token, formData.password);
      setSuccess(true);
      setTimeout(() => navigate('/login'), 2000);
    } catch (err) {
      setError(err.response?.data?.message || "Erro ao redefinir a senha.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={`login-container ${isDarkMode ? 'dark-theme' : 'light-theme'}`}>

      <div className="form-block">
        <div className="login-form-card">

          <div className="logo-block">
            <img src={TimerBookLogo} alt="Logo" />
            <h1>TimerBook</h1>
          </div>

          <p className="welcome-text">Crie uma nova senha</p>

          {success && <div className="status-message status-success">✓ Senha alterada! Redirecionando...</div>}
          {error && <div className="status-message status-error">✗ {error}</div>}

          <form className="profile-form" onSubmit={handleSubmit}>

            {/* SENHA */}
            <div className="form-group">
              <label>Nova senha</label>

              <div style={{ position: "relative" }}>
                <input
                  type={mostrarSenha ? "text" : "password"}
                  name="password"
                  value={formData.password}
                  onChange={handleInputChange}
                  required
                  minLength="6"
                  placeholder="Digite a nova senha"
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
                    //OLHO FECHADO 
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke={eyeColor} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <path d="M17.94 17.94A10.94 10.94 0 0 1 12 20C5 20 1 12 1 12A21.77 21.77 0 0 1 6.06 6.06" />
                      <path d="M9.9 4.24A10.94 10.94 0 0 1 12 4C19 4 23 12 23 12A21.77 21.77 0 0 1 17.94 17.94" />
                      <path d="M1 1L23 23" />
                    </svg>
                  ) : (
                    //OLHO ABERTO
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke={eyeColor} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <path d="M1 12C1 12 5 4 12 4C19 4 23 12 23 12C23 12 19 20 12 20C5 20 1 12 1 12Z" />
                      <circle cx="12" cy="12" r="3" />
                    </svg>
                  )}
                </button>
              </div>
            </div>

            {/* CONFIRMAR SENHA */}
            <div className="form-group" style={{ marginTop: "20px" }}>
              <label>Confirmar senha</label>

              <div style={{ position: "relative" }}>
                <input
                  type={mostrarConfirm ? "text" : "password"}
                  name="confirmPassword"
                  value={formData.confirmPassword}
                  onChange={handleInputChange}
                  required
                  placeholder="Repita a senha"
                  style={{ width: "100%", paddingRight: "40px" }}
                />

                <button
                  type="button"
                  onClick={() => setMostrarConfirm(!mostrarConfirm)}
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
                  {mostrarConfirm ? (
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke={eyeColor} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <path d="M17.94 17.94A10.94 10.94 0 0 1 12 20C5 20 1 12 1 12A21.77 21.77 0 0 1 6.06 6.06" />
                      <path d="M9.9 4.24A10.94 10.94 0 0 1 12 4C19 4 23 12 23 12A21.77 21.77 0 0 1 17.94 17.94" />
                      <path d="M1 1L23 23" />
                    </svg>
                  ) : (
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke={eyeColor} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <path d="M1 12C1 12 5 4 12 4C19 4 23 12 23 12C23 12 19 20 12 20C5 20 1 12 1 12Z" />
                      <circle cx="12" cy="12" r="3" />
                    </svg>
                  )}
                </button>
              </div>
            </div>

            <button
              type="submit"
              className={`btn-concluir ${loading ? 'disabled' : ''}`}
              disabled={loading || success}
              style={{ width: "100%", marginTop: "30px" }}
            >
              {loading ? "Salvando..." : "Salvar nova senha"}
            </button>

            <div className="form-footer-links">
              <a href="/login">Voltar para o Login</a>
            </div>

          </form>
        </div>
      </div>

      {/* DARK MODE OLHO*/}
      <div className="login-theme-toggle">
        <button className="action-icon-btn" onClick={() => setIsDarkMode(!isDarkMode)}>
          <img
            src={isDarkMode ? SunIcon : MoonIcon}
            alt="Alternar Tema"
            className="nav-icon"
            style={{
              filter: isDarkMode ? "invert(1)" : "invert(0)"
            }}
          />
        </button>
      </div>

    </div>
  );
}