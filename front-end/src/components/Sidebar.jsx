import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { getSessionsByReadingId, startReading } from '../features/books/readSessions.js'; 
// 👇 Importando a função getUser
import { getUser } from '../features/user/userApi.js';

import logoImg from '../assets/Home/TimerbookLogo.svg';
import homeIcon from '../assets/Home/HomeIcon.svg';
import BookIcon from '../assets/Home/BookIcon.svg';
import ProfileIcon from '../assets/Home/ProfileIcon.svg';
import ConfigIcon from '../assets/Home/ConfigIcon.svg';
import MoonIcon from '../assets/Home/MoonIcon.svg';
import SunIcon from '../assets/Home/SunIcon.svg';
import '../styles/Sidebar.css';

const Sidebar = ({ menuAtivo, books = [], isDarkMode, setIsDarkMode, onOpenModal }) => {
  const navigate = useNavigate();
  const [isConfigOpen, setIsConfigOpen] = useState(false);

  const handleReadShortcut = async (book) => {
    try {
      const userResponse = await getUser();
      const userId = userResponse.data?.id || userResponse.id;

      const readingResponse = await startReading(userId, book.id);
      const readingId = readingResponse.id;
      const sessions = await getSessionsByReadingId(readingId);
      const sortedSessions = [...sessions].sort((a, b) => new Date(b.startedAt) - new Date(a.startedAt));
      const lastSession = sortedSessions[1];
      const currentSession = sortedSessions[0];
      
      let startPage = 1;
      if (lastSession) {
        startPage = lastSession.endPage;
      }
      
      navigate("/leitor", { state: { book, sessionId: currentSession?.id, initialPage: startPage } });
    } catch (err) {
      console.error("Erro ao iniciar leitura pelo atalho:", err);
      alert("Ops! Erro ao tentar abrir o livro: " + err.message);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("refreshToken");
    navigate("/login"); 
  };

  return (
    <aside className="sidebar">
      <div className="sidebar-header">
        <img src={logoImg} alt="Logo TimerBook" className="logo-icon" />
        <span className="logo-text">TimerBook</span>
      </div>
      
      <nav className="sidebar-nav">
        <Link to="/" className={`nav-item ${menuAtivo === 'inicio' ? 'active' : ''}`}>
          <img src={homeIcon} alt="Início" className="nav-icon" /> Início
        </Link>
        
        <Link to="/meus-livros" className={`nav-item ${menuAtivo === 'livros' ? 'active' : ''}`}>
          <img src={BookIcon} alt="Livros" className="nav-icon" /> Biblioteca
        </Link>

        {books.length === 0 ? (
          <div className="empty-books-msg" style={{marginTop: '5px', marginBottom: '15px'}}>Sem livros cadastrados</div>
        ) : (
          <div className="sidebar-shortcuts" style={{marginTop: '5px', marginBottom: '15px'}}>
            <span style={{fontSize: '0.8rem', color: '#888', marginLeft: '10px'}}>Recentes:</span>
            {books.slice(0, 5).map((book) => (
              <div 
                key={book.id} 
                className="sidebar-shortcut-item" 
                style={{ cursor: 'pointer' }}
                onClick={() => handleReadShortcut(book)}
              >
                {book.name}
              </div>
            ))}
          </div>
        )}

        <Link to="/perfil" className={`nav-item ${menuAtivo === 'perfil' ? 'active' : ''}`}>
          <img src={ProfileIcon} alt="Perfil" className="nav-icon" /> Perfil
        </Link>
      </nav>
      
      <div className="sidebar-footer">
        {onOpenModal && (
          <button className="btn-add-book sidebar-btn-add" onClick={onOpenModal}>
            Adicionar
          </button>
        )}
        
        <div style={{ position: 'relative' }}>
          <button 
            className="action-icon-btn" 
            onClick={() => setIsConfigOpen(!isConfigOpen)}
          >
            <img src={ConfigIcon} alt="Configurações" className="nav-icon" />
          </button>

          {isConfigOpen && (
            <div className="config-popover">
              <button className="popover-item logout-btn" onClick={handleLogout}>
                Sair
              </button>
            </div>
          )}
        </div>
        
        <button className="action-icon-btn" onClick={() => setIsDarkMode(!isDarkMode)}>
          <img src={isDarkMode ? SunIcon : MoonIcon} alt="Aparência" className="nav-icon" />
        </button>
      </div>
    </aside>
  );
};

export default Sidebar;