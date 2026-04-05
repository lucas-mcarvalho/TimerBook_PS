import React from 'react';
import logoImg from '../assets/Home/TimerbookLogo.svg';
import homeIcon from '../assets/Home/HomeIcon.svg';
import BookIcon from '../assets/Home/BookIcon.svg';
import ProfileIcon from '../assets/Home/ProfileIcon.svg';
import ConfigIcon from '../assets/Home/ConfigIcon.svg';
import MoonIcon from '../assets/Home/MoonIcon.svg';
import SunIcon from '../assets/Home/SunIcon.svg';

const Sidebar = ({ menuAtivo, setMenuAtivo, books, onRead, isDarkMode, setIsDarkMode }) => {
  return (
    <aside className="sidebar">
      <div className="sidebar-header">
        <img src={logoImg} alt="Logo TimerBook" className="logo-icon" />
        <span className="logo-text">TimerBook</span>
      </div>
      
      <nav className="sidebar-nav">
        <div
          className={`nav-item ${menuAtivo === 'inicio' ? 'active' : ''}`}
          onClick={() => setMenuAtivo('inicio')}
        >
          <img src={homeIcon} alt="Início" className="nav-icon" /> Início
        </div>
        
        <div
          className={`nav-item ${menuAtivo === 'livros' ? 'active' : ''}`}
          onClick={() => setMenuAtivo('livros')}
        >
          <img src={BookIcon} alt="Livros" className="nav-icon" /> Biblioteca
        </div>

        {books.length > 0 && (
          <div className="sidebar-shortcuts" style={{ marginBottom: '25px' }}>
            <span style={{ fontSize: '0.8rem', color: '#888', marginLeft: '10px' }}>
              Recentes:
            </span>
            {books.slice(0, 3).map((book) => (
              <div
                key={book.id}
                className="sidebar-shortcut-item"
                onClick={() => onRead(book)}
                style={{ cursor: 'pointer' }}
              >
                {book.name}
              </div>
            ))}
          </div>
        )}

        <div
          className={`nav-item ${menuAtivo === 'perfil' ? 'active' : ''}`}
          onClick={() => setMenuAtivo('perfil')}
        >
          <img src={ProfileIcon} alt="Perfil" className="nav-icon" /> Perfil
        </div>
      </nav>
      
      <div className="sidebar-footer">
        <button className="action-icon-btn">
          <img src={ConfigIcon} alt="Configurações" className="nav-icon" />
        </button>
        
        <button className="action-icon-btn" onClick={() => setIsDarkMode(!isDarkMode)}>
          <img src={isDarkMode ? SunIcon : MoonIcon} alt="Aparência" className="nav-icon" />
        </button>
      </div>
    </aside>
  );
};

export default Sidebar;