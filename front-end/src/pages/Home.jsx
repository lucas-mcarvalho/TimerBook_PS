import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getBooks } from "../features/books/booksApi.js"; 

import '../styles/Layout.css'; 
import '../styles/Waves.css'; 
import '../styles/Home.css';
import '../styles/HomeDark.css'; 
import Sidebar from '../components/Sidebar';
import ProfileIcon from '../assets/Home/ProfileIcon.svg';

const Home = () => {
  const [books, setBooks] = useState([]);

  const [isDarkMode, setIsDarkMode] = useState(() => {
    const savedTheme = localStorage.getItem('timerbook-theme');
    return savedTheme === 'dark';
  });

  useEffect(() => {
    localStorage.setItem('timerbook-theme', isDarkMode ? 'dark' : 'light');
  }, [isDarkMode]);

  useEffect(() => {
    const fetchBooks = async () => {
      try {
        const booksData = await getBooks();
        setBooks(booksData);
      } catch (err) {
        console.error("Erro ao carregar livros na Home:", err);
      }
    };

    fetchBooks();
  }, []);

  return (
    <div className={`dashboard-container ${isDarkMode ? 'dark-theme' : ''}`}>
      
      <Sidebar 
        menuAtivo="inicio" 
        books={books} 
        isDarkMode={isDarkMode} 
        setIsDarkMode={setIsDarkMode} 
      />

      <main className="main-content welcome-container">
        
        <div className="waves-container">
          <svg className="ribbon-svg" viewBox="0 0 1000 300" preserveAspectRatio="none">
            <defs>
              <linearGradient id="glassGradient" x1="0%" y1="0%" x2="100%" y2="0%">
                <stop offset="0%" stopColor="white" stopOpacity="0.6" />
                <stop offset="50%" stopColor="white" stopOpacity="0.1" />
                <stop offset="100%" stopColor="white" stopOpacity="0.6" />
              </linearGradient>
            </defs>

            <path className="ribbon ribbon1" 
                  d="M-100,150 C200,-50 600,350 1100,100 L1100,120 C600,380 200,-10 -100,170 Z" 
                  fill="url(#glassGradient)" />
            
            <path className="ribbon ribbon2" 
                  d="M-100,200 C400,400 600,-100 1100,200 L1100,220 C600,-60 400,450 -100,210 Z" 
                  fill="url(#glassGradient)" />
            
            <path className="ribbon ribbon3" 
                  d="M-100,100 C250,250 800,250 1100,100 L1100,105 C800,280 250,280 -100,120 Z" 
                  fill="url(#glassGradient)" />
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
            <Link to="/meus-livros" className="btn-go-library">
              Acessar Minha Biblioteca
            </Link>
          </div>
        </div>

      </main>
      
    </div>
  );
};

export default Home;