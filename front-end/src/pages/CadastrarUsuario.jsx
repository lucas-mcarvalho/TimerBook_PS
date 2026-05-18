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

  // Estado para mostrar a pré-visualização da imagem
  const [photoPreview, setPhotoPreview] = useState(null);

  // Estados de feedback
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);
  
  // Estado específico para a lista de erros da senha (agora é um Array)
  const [passwordErrors, setPasswordErrors] = useState([]);

  // Manipulador para os inputs de texto padrão
  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    
    // Limpa os "chips" de erro assim que o usuário volta a digitar a senha
    if (name === "password") {
      setPasswordErrors([]);
    }
  };

  // Manipulador exclusivo para o input de arquivo (foto)
  const handleFileChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setFormData((prev) => ({ ...prev, photo: file }));
      setPhotoPreview(URL.createObjectURL(file));
    } else {
      setFormData((prev) => ({ ...prev, photo: null }));
      setPhotoPreview(null);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (loading) return;

    // Reseta todos os alertas antes de tentar enviar novamente
    setError(null);
    setSuccess(false);
    setPasswordErrors([]);

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
        } 
        // Lógica inteligente para capturar os erros em inglês e traduzir
        else if (lowerMessage.includes("password") || lowerMessage.includes("senha")) {
          const errorsList = [];
          
          if (lowerMessage.includes("8 or more") || lowerMessage.includes("length")) errorsList.push("Mínimo de 8 caracteres");
          if (lowerMessage.includes("uppercase")) errorsList.push("1 letra maiúscula");
          if (lowerMessage.includes("lowercase")) errorsList.push("1 letra minúscula");
          if (lowerMessage.includes("special") || lowerMessage.includes("non-alphanumeric")) errorsList.push("1 caractere especial (!@#)");
          if (lowerMessage.includes("digit") || lowerMessage.includes("number")) errorsList.push("1 número");

          if (errorsList.length > 0) {
            setPasswordErrors(errorsList);
          } else {
            setPasswordErrors(["A senha está muito fraca."]);
          }
        } else {
          setError(message); // Cai aqui se for um erro diferente
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
                style={{ display: "none" }}
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

            {/* CAMPO DE SENHA COM FEEDBACK VISUAL PREMIUM */}
            <div className="form-group" style={{ marginTop: "15px" }}>
              <label>Senha</label>
              <input
                type="password"
                name="password"
                value={formData.password}
                onChange={handleInputChange}
                required
                placeholder="••••••••"
                style={passwordErrors.length > 0 ? { borderColor: '#ff4757', boxShadow: '0 0 0 2px rgba(255, 71, 87, 0.1)' } : {}}
              />
              
              {/* RENDERIZAÇÃO DOS CHIPS DE ERRO */}
              {passwordErrors.length > 0 && (
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px', marginTop: '10px' }}>
                  <span style={{ width: '100%', fontSize: '12px', color: '#ff4757', fontWeight: '500', marginBottom: '2px' }}>
                    Sua senha precisa de:
                  </span>
                  
                  {passwordErrors.map((erroMsg, index) => (
                    <div key={index} style={{
                      display: 'flex', 
                      alignItems: 'center', 
                      gap: '4px',
                      background: 'rgba(255, 71, 87, 0.1)', 
                      color: '#ff4757',
                      padding: '4px 8px', 
                      borderRadius: '6px', 
                      fontSize: '11px',
                      fontWeight: '500',
                      border: '1px solid rgba(255, 71, 87, 0.2)'
                    }}>
                      {/* Ícone de X em SVG */}
                      <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round">
                        <line x1="18" y1="6" x2="6" y2="18"></line>
                        <line x1="6" y1="6" x2="18" y2="18"></line>
                      </svg>
                      {erroMsg}
                    </div>
                  ))}
                </div>
              )}
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