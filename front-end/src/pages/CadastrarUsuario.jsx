import { useState } from "react";
import { registerUser } from "../features/auth/user.js";
import { useNavigate } from "react-router-dom";

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

  // Novo estado para mostrar a pré-visualização da imagem
  const [photoPreview, setPhotoPreview] = useState(null);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);

  // Manipulador para os inputs de texto padrão
  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  // Novo manipulador exclusivo para o input de arquivo (foto)
  const handleFileChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setFormData((prev) => ({ ...prev, photo: file }));
      // Cria uma URL local temporária para o preview da imagem
      setPhotoPreview(URL.createObjectURL(file));
    } else {
      setFormData((prev) => ({ ...prev, photo: null }));
      setPhotoPreview(null);
    }
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
      // Repassando os dados, incluindo a foto, para a sua função de API
      await registerUser(
        formData.username,
        formData.email,
        formData.password,
        formData.photo
      );

      setSuccess("Conta criada com sucesso! Enviamos um link de confirmação para o seu e-mail. Você precisa clicar nele antes de fazer login.");

      // Limpa os formulários e o preview
      setFormData({
        username: "",
        email: "",
        password: "",
        confirmPassword: "",
        photo: null,
      });
      setPhotoPreview(null);
      
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
            <div className="status-message status-success" style={{ padding: '10px', marginBottom: '15px' }}>
              ✓ {success}
            </div>
          )}

          {error && (
            <div className="status-message status-error" style={{ padding: '10px', marginBottom: '15px' }}>
              ✗ {error}
            </div>
          )}

          {/* FORM */}
          <form className="profile-form" onSubmit={handleSubmit}>

            {/* SEÇÃO DA FOTO DE PERFIL */}
            <div className="form-group" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', marginBottom: "15px" }}>
              {photoPreview ? (
                <img 
                  src={photoPreview} 
                  alt="Preview" 
                  style={{ width: "100px", height: "100px", borderRadius: "50%", objectFit: "cover", marginBottom: "10px", border: "2px solid #ccc" }} 
                />
              ) : (
                <div style={{ width: "100px", height: "100px", borderRadius: "50%", backgroundColor: isDarkMode ? "#333" : "#f0f0f0", display: 'flex', justifyContent: 'center', alignItems: 'center', marginBottom: "10px", border: "2px dashed #ccc" }}>
                  <span style={{ fontSize: "12px", color: "#888" }}>Sem foto</span>
                </div>
              )}
              
              <label htmlFor="foto-upload" style={{ cursor: "pointer", color: "var(--primary-color)", textDecoration: "underline", fontSize: "14px" }}>
                Escolher Foto de Perfil
              </label>
              <input
                id="foto-upload"
                type="file"
                accept="image/*"
                onChange={handleFileChange}
                style={{ display: "none" }} // Esconde o input feio nativo do navegador
              />
            </div>

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