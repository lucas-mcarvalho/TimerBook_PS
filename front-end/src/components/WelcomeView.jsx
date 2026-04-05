import React from 'react';
import ProfileIcon from '../assets/Home/ProfileIcon.svg';

const WelcomeView = ({ onGoToLibrary }) => {
  return (
    <div className="welcome-container">
      <div className="waves-container">
        <svg className="ribbon-svg" viewBox="0 0 1000 300" preserveAspectRatio="none">
          <defs>
            <linearGradient id="glassGradient" x1="0%" y1="0%" x2="100%" y2="0%">
              <stop offset="0%" stopColor="white" stopOpacity="0.6" />
              <stop offset="50%" stopColor="white" stopOpacity="0.1" />
              <stop offset="100%" stopColor="white" stopOpacity="0.6" />
            </linearGradient>
          </defs>
          <path className="ribbon ribbon1" d="M-100,150 C200,-50 600,350 1100,100 L1100,120 C600,380 200,-10 -100,170 Z" fill="url(#glassGradient)" />
          <path className="ribbon ribbon2" d="M-100,200 C400,400 600,-100 1100,200 L1100,220 C600,-60 400,450 -100,210 Z" fill="url(#glassGradient)" />
          <path className="ribbon ribbon3" d="M-100,100 C250,250 800,250 1100,100 L1100,105 C800,280 250,280 -100,120 Z" fill="url(#glassGradient)" />
        </svg>
      </div>

      <div className="welcome-content-wrapper">
        <div className="welcome-header">
          <div className="profile-image-container">
            <img src={ProfileIcon} alt="Foto de Perfil" className="profile-image-default" />
          </div>
          <h1 className="welcome-title">Bem-vindo de volta!</h1>
          <p className="welcome-subtitle">Usuário</p>
        </div>
        <div className="welcome-actions">
          <button className="btn-go-library" onClick={onGoToLibrary}>
            Acessar Minha Biblioteca
          </button>
        </div>
      </div>
    </div>
  );
};

export default WelcomeView;