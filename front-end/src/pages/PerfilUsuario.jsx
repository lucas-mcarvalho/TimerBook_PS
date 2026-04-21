import { useState, useEffect } from "react";
// 👇 NOVO: Importando o useNavigate para fazer o redirecionamento
import { useNavigate } from "react-router-dom"; 
import { getUser } from "../features/user/userApi.js"; 
import { getBooks } from "../features/books/booksApi.js"; 
import Sidebar from '../components/Sidebar';
import EditProfileModal from '../components/EditProfileModal';
import ProfileIcon from '../assets/Home/ProfileIcon.svg'; 

import "../styles/PerfilUsuario.css";
import '../styles/Layout.css'; 

export default function PerfilUsuario() {
  const [isDarkMode, setIsDarkMode] = useState(() => {
    const savedTheme = localStorage.getItem('timerbook-theme');
    return savedTheme === 'dark';
  });

  const [userInfo, setUserInfo] = useState(null);
  const [books, setBooks] = useState([]);
  const [fetching, setFetching] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [successMessage, setSuccessMessage] = useState("");
  
  // 👇 NOVO: Inicializando o navigate
  const navigate = useNavigate(); 

  useEffect(() => {
    localStorage.setItem('timerbook-theme', isDarkMode ? 'dark' : 'light');
  }, [isDarkMode]);

  useEffect(() => {
    async function fetchData() {
      try {
        const userData = await getUser();
        setUserInfo(userData.data || userData);

        const booksData = await getBooks();
        setBooks(booksData);

      } catch (err) {
        console.error("Erro ao carregar dados do perfil:", err);
      } finally {
        setFetching(false);
      }
    }
    fetchData();
  }, []);

  const handleUpdateSuccess = (updatedData) => {
    setUserInfo((prev) => ({ ...prev, ...updatedData }));
    setSuccessMessage("Perfil atualizado com sucesso!");
    setTimeout(() => setSuccessMessage(""), 4000); 
  };

  if (fetching) {
    return (
      <div className={`dashboard-container ${isDarkMode ? 'dark-theme' : ''}`}>
        <Sidebar menuAtivo="perfil" books={books} isDarkMode={isDarkMode} setIsDarkMode={setIsDarkMode} />
        <main className="main-content" style={{ display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
          <h2>Carregando perfil...</h2>
        </main>
      </div>
    );
  }

  return (
    <div className={`dashboard-container ${isDarkMode ? 'dark-theme' : ''}`}>
      <Sidebar menuAtivo="perfil" books={books} isDarkMode={isDarkMode} setIsDarkMode={setIsDarkMode} />
      
      <main className="main-content">
        <h1>Meu Perfil</h1>

        <div className="profile-container">
          {successMessage && <div className="status-message status-success">✓ {successMessage}</div>}

          {userInfo && (
            <div className="info-card">
              
              <div className="profile-image-container">
                <img 
                  src={
                    (userInfo?.photopath || userInfo?.photo) 
                    ? `http://localhost:8080/${userInfo.photopath || userInfo.photo}?t=${Date.now()}` 
                    : ProfileIcon
                  } 
                  alt="Foto" 
                  className={(userInfo?.photopath || userInfo?.photo) ? "" : "profile-image-default"}
                  onError={(e) => {
                    e.target.onerror = null; 
                    e.target.src = ProfileIcon;
                    e.target.className = "profile-image-default";
                  }}
                />
              </div>

              <div className="profile-details">
                <h2>Informações da Conta</h2>
                <p><strong>Nome de Usuário:</strong> {userInfo.username}</p>
                <p><strong>Email:</strong> {userInfo.email}</p>
              </div>

            </div>
          )}
        </div>

        {/* 👇 MODIFICADO: Adicionado gap para separar os botões e o novo botão de senha */}
        <div className="bottom-actions" style={{ display: 'flex', gap: '15px' }}>
          <button className="btn-add-book" onClick={() => setIsModalOpen(true)}>
            Editar Perfil
          </button>
          
          <button 
            className="btn-secondary" 
            onClick={() => navigate('/esqueceu-senha')}
          >
            Redefinir Senha
          </button>
          <div style={{ visibility:"hidden", display: 'flex', gap: '15px' }}>
            <button onClick={() => navigate('/perfil/editar')} className="btn-secondary">
              deletar conta
            </button>
          </div>
        </div>
      </main>

      <EditProfileModal 
        isOpen={isModalOpen} 
        onClose={() => setIsModalOpen(false)} 
        userInfo={userInfo}
        onUpdateSuccess={handleUpdateSuccess}
      />
    </div>
  );
}