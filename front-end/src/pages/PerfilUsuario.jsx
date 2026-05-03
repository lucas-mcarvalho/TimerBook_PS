import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom"; 
import { useToast } from "../components/ToastContext.js"; 
import { getUser, deleteUser, updateReadingGoal } from "../features/user/userApi.js"; 
import { getBookByUserId } from "../features/books/booksApi.js"; 
import Sidebar from '../components/Sidebar';
import EditProfileModal from '../components/EditProfileModal';
import AchievementsList from '../components/AchievementsList'; 
import ProfileIcon from '../assets/Home/ProfileIcon.svg'; 
import { getProfilePhotoPath, resolveProfilePhotoUrl } from '../utils/profileImage.js';

import "../styles/PerfilUsuario.css";
import '../styles/Layout.css'; 

export default function PerfilUsuario() {
  const { showToast } = useToast();
  const [isDarkMode, setIsDarkMode] = useState(() => {
    const savedTheme = localStorage.getItem('timerbook-theme');
    return savedTheme === 'dark';
  });

  const [userInfo, setUserInfo] = useState(null);
  const [books, setBooks] = useState([]);
  const [fetching, setFetching] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [isGoalModalOpen, setIsGoalModalOpen] = useState(false);
  const [selectedReadingGoal, setSelectedReadingGoal] = useState(10);
  const [savingReadingGoal, setSavingReadingGoal] = useState(false);
  const [successMessage, setSuccessMessage] = useState("");
  const [error, setError] = useState(null);
  
  const navigate = useNavigate(); 

  useEffect(() => {
    localStorage.setItem('timerbook-theme', isDarkMode ? 'dark' : 'light');
  }, [isDarkMode]);

  useEffect(() => {
    async function fetchData() {
      try {
        setError(null);
        const userData = await getUser();
        const info = userData.data || userData;
        setUserInfo(info);
        setSelectedReadingGoal(info.dailyReadingGoalMinutes || 10);

        const userId = info.id;
        const booksData = await getBookByUserId(userId);
        setBooks(booksData);

      } catch (err) {
        console.error("Erro ao carregar dados do perfil:", err);
        setError("Não foi possível carregar as informações do seu perfil. Tente atualizar a página.");
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

  const handleOpenGoalModal = () => {
    setSelectedReadingGoal(userInfo?.dailyReadingGoalMinutes || 10);
    setIsGoalModalOpen(true);
  };

  const handleUpdateReadingGoal = async () => {
    if (!selectedReadingGoal || savingReadingGoal) return;

    setSavingReadingGoal(true);
    try {
      const response = await updateReadingGoal(selectedReadingGoal);
      const dailyReadingGoalMinutes = response?.dailyReadingGoalMinutes || selectedReadingGoal;

      setUserInfo((prev) => ({ ...prev, dailyReadingGoalMinutes }));
      setSelectedReadingGoal(dailyReadingGoalMinutes);
      setIsGoalModalOpen(false);
      setSuccessMessage("Meta de leitura atualizada com sucesso!");
      setTimeout(() => setSuccessMessage(""), 4000);
    } catch (err) {
      console.error("Erro ao atualizar meta de leitura:", err);
      showToast("Erro ao atualizar a meta de leitura. Tente novamente.", "error");
    } finally {
      setSavingReadingGoal(false);
    }
  };

  const handleDeleteAccount = async () => {
    try {
      if (userInfo?.id) {
        await deleteUser(userInfo.id);
        
        // Limpa tokens e redireciona para a tela de login
        localStorage.removeItem("token");
        localStorage.removeItem("refreshToken");
        navigate("/"); 
      }
    } catch (err) {
      console.error("Erro ao excluir conta:", err);
      showToast("Erro ao excluir a conta. Tente novamente mais tarde.", "error");
    } finally {
      setIsDeleteModalOpen(false);
    }
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

  const profilePhotoPath = getProfilePhotoPath(userInfo);
  const profilePhotoUrl = resolveProfilePhotoUrl(profilePhotoPath, { cacheBust: true });

  return (
    <div className={`dashboard-container ${isDarkMode ? 'dark-theme' : ''}`}>
      <Sidebar menuAtivo="perfil" books={books} isDarkMode={isDarkMode} setIsDarkMode={setIsDarkMode} />
      
      <main className="main-content">
        <h1>Meu Perfil</h1>

        <div className="profile-container">
          {successMessage && <div className="status-message status-success">✓ {successMessage}</div>}
          {error && <div className="status-message status-error">✗ {error}</div>}

          {userInfo && (
            <>
              <div className="info-card">
                <div className="profile-image-container">
                  <img 
                    src={profilePhotoUrl || ProfileIcon} 
                    alt="Foto" 
                    className={profilePhotoUrl ? "" : "profile-image-default"}
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
                  <p><strong>Meta diária:</strong> {userInfo.dailyReadingGoalMinutes || 10} minutos</p>
                </div>
              </div>

              {userInfo.id && <AchievementsList userId={userInfo.id} />}
            </>
          )}
        </div>

        <div className="bottom-actions" style={{ display: 'flex', gap: '15px', flexWrap: 'wrap' }}>
          <button className="btn-add-book" onClick={() => setIsModalOpen(true)}>
            Editar Perfil
          </button>

          <button
            id="guide-reading-goal-button"
            className="btn-secondary"
            onClick={handleOpenGoalModal}
          >
            Alterar Meta de Leitura
          </button>
          
          <button 
            className="btn-secondary" 
            onClick={() => navigate('/esqueceu-senha')}
          >
            Redefinir Senha
          </button>

          <button 
            className="btn-secondary btn-delete-account" 
            onClick={() => setIsDeleteModalOpen(true)}
          >
            Deletar Conta
          </button>
        </div>
      </main>

      <EditProfileModal 
        isOpen={isModalOpen} 
        onClose={() => setIsModalOpen(false)} 
        userInfo={userInfo}
        onUpdateSuccess={handleUpdateSuccess}
      />

      {isGoalModalOpen && (
        <div className="reading-goal-overlay">
          <div className="reading-goal-modal">
            <h3>Meta diária de leitura</h3>
            <p>Escolha quantos minutos você quer ler por dia.</p>

            <div className="reading-goal-options">
              {[10, 20, 30].map((minutes) => (
                <button
                  key={minutes}
                  type="button"
                  className={`reading-goal-option ${selectedReadingGoal === minutes ? "selected" : ""}`}
                  onClick={() => setSelectedReadingGoal(minutes)}
                  disabled={savingReadingGoal}
                >
                  <strong>{minutes}</strong>
                  <span>min/dia</span>
                </button>
              ))}
            </div>

            <div className="reading-goal-actions">
              <button
                className="btn-secondary"
                onClick={() => setIsGoalModalOpen(false)}
                disabled={savingReadingGoal}
              >
                Cancelar
              </button>
              <button
                className="btn-save-goal"
                onClick={handleUpdateReadingGoal}
                disabled={savingReadingGoal}
              >
                {savingReadingGoal ? "Salvando..." : "Salvar Meta"}
              </button>
            </div>
          </div>
        </div>
      )}

      {isDeleteModalOpen && (
        <div className="delete-confirmation-overlay">
          <div className="delete-confirmation-balloon">
            <h3>Confirmação de Exclusão</h3>
            <p>Deseja realmente excluir sua conta definitivamente? Esta ação não pode ser desfeita.</p>
            <div className="delete-modal-actions">
              <button className="btn-secondary" onClick={() => setIsDeleteModalOpen(false)}>Cancelar</button>
              <button className="btn-confirm-delete" onClick={handleDeleteAccount}>Excluir Definitivamente</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
