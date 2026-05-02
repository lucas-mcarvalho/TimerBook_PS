import { useState } from "react";
import { registerUser } from "../features/auth/user.js";
import { useNavigate} from "react-router-dom";

import TimerBookLogo from "../assets/Home/TimerbookLogo.svg";

import "../styles/Login.css";
import "../styles/LoginLight.css";
import "../styles/HomeDark.css";

export default function CadastrarUsuario() {
  const navigate = useNavigate();
  const [isDarkMode, setIsDarkMode] = useState(() => {
    const saved = localStorage.getItem("timerbook-theme");
    return saved === "dark";
  });

  const [formData, setFormData] = useState({
    username: "",
    email: "",
    password: "",
    confirmPassword: "",
    photo: null,
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };


  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (loading) return;

    setError(null);
    setSuccess(false);

    if (formData.password !== formData.confirmPassword) {
      setError("As senhas não coincidem.");
      return; 
    }

    setLoading(true);

    try {
      
      await registerUser(
        formData.username,
        formData.email,
        formData.password,
        formData.photo
      );

      setSuccess("Conta criada com sucesso! Enviamos um link de confirmação para o seu e-mail. Você precisa clicar nele antes de fazer login.");

      setFormData({
        username: "",
        email: "",
        password: "",
        confirmPassword: "",
        photo: null,
      });
      
    } catch (err) {
      const status = err.response?.status;
      const message = err.response?.data;

      if (status === 400 && typeof message === 'string') {
        const lowerMessage = message.toLowerCase();
        
        if (lowerMessage.includes("email já cadastrado") || lowerMessage.includes("email ja cadastrado")) {
          setError("Este e-mail já está sendo usado. Tente outro ou recupere sua senha.");
        } else if (lowerMessage.includes("username já está em uso") || lowerMessage.includes("username ja esta em uso")) {
          setError("Este nome de usuário já existe. Escolha um nome diferente.");
        } else {
          setError(message);
        }
      } else {
        setError("Erro ao conectar com o servidor. Tente novamente mais tarde.");
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={`login-container ${isDarkMode ? "dark-theme" : "light-theme"}`}>

      <div className="form-block">
        <div className="login-form-card">

          {/* LOGO */}
          <div className="logo-block">
            <img src={TimerBookLogo} alt="TimerBook" />
            <h1>TimerBook</h1>
          </div>

          <p className="welcome-text">Crie sua conta</p>

          {success && (
            <div className="status-message status-success">
              ✓ Conta criada com sucesso! Enviamos um link de confirmação para o seu e-mail. Você precisa clicar nele antes de fazer login.
            </div>
          )}

          {error && (
            <div className="status-message status-error">
              ✗ {error}
            </div>
          )}

          {/* FORM */}
          <form className="profile-form" onSubmit={handleSubmit}>

            <div className="form-group">
              <label>Username</label>
              <input
                type="text"
                name="username"
                value={formData.username}
                onChange={handleInputChange}
                required
                placeholder="Seu nome"
              />
            </div>

            <div className="form-group" style={{ marginTop: "15px" }}>
              <label>Email</label>
              <input
                type="email"
                name="email"
                value={formData.email}
                onChange={handleInputChange}
                required
                placeholder="seu@email.com"
              />
            </div>

            <div className="form-group" style={{ marginTop: "15px" }}>
              <label>Senha</label>
              <input
                type="password"
                name="password"
                value={formData.password}
                onChange={handleInputChange}
                required
                placeholder="••••••••"
              />
            </div>

            <div className="form-group" style={{ marginTop: "15px" }}>
              <label>Confirmar senha</label>
              <input
                type="password"
                name="confirmPassword"
                value={formData.confirmPassword}
                onChange={handleInputChange}
                required
                placeholder="Repita a senha"
              />
            </div>

            <button
              type="submit"
              className={`btn-concluir ${loading ? "disabled" : ""}`}
              disabled={loading}
              style={{ width: "100%", marginTop: "25px" }}
            >
              {loading ? "Cadastrando..." : "Cadastrar"}

            </button>

          </form>

          <div className="form-footer-links">
            <a href="/">Já tenho uma conta</a>
          </div>

        </div>
      </div>

    </div>
  );
}