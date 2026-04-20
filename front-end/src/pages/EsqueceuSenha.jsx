import { useState, useEffect } from "react";
import { passwordRecoveryService } from "../features/auth/passwordRecoveryService.js";

import TimerBookLogo from '../assets/Home/TimerbookLogo.svg'; 
import MoonIcon from '../assets/Home/MoonIcon.svg';
import SunIcon from '../assets/Home/SunIcon.svg';

import "../styles/Login.css";
import "../styles/LoginLight.css"; 
import '../styles/HomeDark.css'; 

export default function EsqueceuSenha() {
  const [isDarkMode, setIsDarkMode] = useState(() => {
    const savedTheme = localStorage.getItem('timerbook-theme');
    return savedTheme === 'dark';
  });

  useEffect(() => {
    localStorage.setItem('timerbook-theme', isDarkMode ? 'dark' : 'light');
  }, [isDarkMode]);

  const [formData, setFormData] = useState({
    email: "",
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (loading) return;

    setLoading(true);
    setError(null);
    setSuccess(null);

    try {
      const response = await passwordRecoveryService.requestRecovery(formData.email);
      setSuccess(response.message || "E-mail de recuperação enviado com sucesso!");
      setFormData({ email: "" });
    } catch (err) {
      setError(err.response?.data?.message || "Erro ao solicitar recuperação.");
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

          <p className="welcome-text">Recuperar acesso à sua conta</p>

          {success && <div className="status-message status-success">✓ {success}</div>}
          {error && <div className="status-message status-error">✗ {error}</div>}

          <form className="profile-form">
            <div className="form-group">
              <label>Email cadastrado</label>
              <input
                type="email"
                name="email"
                value={formData.email}
                onChange={handleInputChange}
                required
                placeholder="seu.email@exemplo.com"
              />
            </div>

            <button
              type="submit"
              onClick={handleSubmit}
              className={`btn-concluir ${loading ? 'disabled' : ''}`}
              disabled={loading}
              style={{ width: "100%", marginTop: "30px" }}
            >
              {loading ? "Enviando..." : "Enviar link de recuperação"}
            </button>

            <div className="form-footer-links">
              <a href="/">Voltar para o Login</a>
            </div>
          </form>

        </div>
      </div>

      <div className="login-theme-toggle">
        <button className="action-icon-btn" onClick={() => setIsDarkMode(!isDarkMode)}>
        <img
          src={isDarkMode ? SunIcon : MoonIcon}
          alt="Alternar Tema"
          className="nav-icon"
          style={{
            filter: isDarkMode ? "invert(1)" : "invert(0)"
          }}
        /></button>
      </div>

    </div>
  );
}