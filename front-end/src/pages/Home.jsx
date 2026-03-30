import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import '../styles/Home.css';
import '../styles/HomeDark.css'; 
import logoImg from '../assets/Home/TimerbookLogo.svg';
import homeIcon from '../assets/Home/HomeIcon.svg';
import BookIcon from '../assets/Home/BookIcon.svg';
import ProfileIcon from '../assets/Home/ProfileIcon.svg';
import ConfigIcon from '../assets/Home/ConfigIcon.svg';
import MoonIcon from '../assets/Home/MoonIcon.svg';
import SunIcon from '../assets/Home/SunIcon.svg';
import PencilIcon from '../assets/Home/PencilIcon.svg';

import HomeAddBookModal from '../components/HomeAddBookModal';
import { getBooks, deleteBook } from '../features/books/booksApi.js';

const Home = () => {
  const [menuAtivo, setMenuAtivo] = useState('inicio');
  const [books, setBooks] = useState([]);

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  
  const [isDarkMode, setIsDarkMode] = useState(() => {
    const savedTheme = localStorage.getItem('timerbook-theme');
    return savedTheme === 'dark';
  });

  useEffect(() => {
    localStorage.setItem('timerbook-theme', isDarkMode ? 'dark' : 'light');
  }, [isDarkMode]);

  useEffect(() => {
    async function fetchMyBooks() {
      try {
        const livrosDoBanco = await getBooks();
        setBooks(livrosDoBanco);
      } catch (error) {
        console.error("Não consegui puxar os livros:", error);
      }
    }
    fetchMyBooks();
  }, []);

  const handleDeleteBook = async (idToRemove) => {
    try {
      await deleteBook(idToRemove);
      setBooks(prev => prev.filter(book => book.id !== idToRemove));
    } catch (error) {
      console.error("Erro ao apagar o livro:", error);
      alert("Ops! Não consegui apagar o livro. O servidor pode estar fora do ar.");
    }
  };

  const handleAddNewBook = (serverBook) => {
    setBooks(prev => [...prev, serverBook]);
  };

  return (
    <div className={`dashboard-container ${isDarkMode ? 'dark-theme' : ''}`}>
      <aside className="sidebar">
        <div className="sidebar-header">
          <img src={logoImg} alt="Logo TimerBook" className="logo-icon" />
          <span className="logo-text">TimerBook</span>
        </div>
        
        <nav className="sidebar-nav">
          <Link to="/" className={`nav-item ${menuAtivo === 'inicio' ? 'active' : ''}`} onClick={() => setMenuAtivo('inicio')}>
            <img src={homeIcon} alt="Início" className="nav-icon" /> Início
          </Link>
          
          <Link to="/meus-livros" className={`nav-item ${menuAtivo === 'livros' ? 'active' : ''}`} onClick={() => setMenuAtivo('livros')}>
            <img src={BookIcon} alt="Livros" className="nav-icon" /> Biblioteca
          </Link>

          <Link to="/cadastrar-livro" className={`nav-item ${menuAtivo === 'cadastro' ? 'active' : ''}`} onClick={() => setMenuAtivo('cadastro')}>
            <img src={PencilIcon} alt="Cadastrar" className="nav-icon" /> Adicionar Livro
          </Link>

          <Link to="/leitor" className={`nav-item ${menuAtivo === 'leitor' ? 'active' : ''}`} onClick={() => setMenuAtivo('leitor')}>
            <img src={BookIcon} alt="Leitor" className="nav-icon" /> Leitor PDF
          </Link>

          {books.length === 0 ? (
            <div className="empty-books-msg" style={{marginTop: '20px'}}>Sem livros cadastrados</div>
          ) : (
            <div className="sidebar-shortcuts" style={{marginTop: '20px'}}>
              <span style={{fontSize: '0.8rem', color: '#888', marginLeft: '10px'}}>Recentes:</span>
              {books.slice(0, 5).map((book) => (
                <Link to="/leitor" state={{ book: book }} key={book.id} className="sidebar-shortcut-item">
                  {book.name}
                </Link>
              ))}
            </div>
          )}
        </nav>
        
        <div className="sidebar-footer">
          <button className="action-icon-btn"><img src={ConfigIcon} alt="Configurações" className="nav-icon" /></button>
          
          <button className="action-icon-btn" onClick={() => setIsDarkMode(!isDarkMode)}>
            <img src={isDarkMode ? SunIcon : MoonIcon} alt="Aparência" className="nav-icon" />
          </button>
        </div>
      </aside>

      <main className="main-content">
        <h1>Sua Biblioteca</h1>
        <div className="books-grid">
          {books.length === 0 ? (
            <button className="book-card add-new-card" onClick={() => setIsModalOpen(true)} style={{border: 'none', background: 'transparent'}}>
              <div className="book-cover-wrapper"><div className="book-cover-placeholder">+</div></div>
              <div className="book-info"><h3 className="book-title">Adicionar novo livro</h3></div>
            </button>
          ) : (
            books.map((book) => (
              <Link to="/leitor" state={{ book: book }} key={book.id} className="book-card">
                {isEditing && (
                  <button 
                    className="btn-delete-book"
                    onClick={(e) => { 
                      e.preventDefault(); 
                      e.stopPropagation(); 
                      handleDeleteBook(book.id); 
                    }}
                  >
                    X
                  </button>
                )}
                
                <div className="book-cover-wrapper">
                  {(book.cover || book.coverUrl) && (
                    <img 
                      src={book.cover?.startsWith('blob:') 
                        ? book.cover 
                        : `http://localhost:8080/${book.coverUrl}`} 
                      alt={`Capa de ${book.name}`} 
                      className="book-cover-image" 
                      onError={(e) => {
                        e.target.style.display = 'none';
                      }}
                    />
                  )}
                  <div className="book-cover-placeholder">{book.name ? book.name.charAt(0) : '?'}</div>
                </div>

                <div className="book-info">
                  <h3 className="book-title">{book.name}</h3>
                  <span className="book-year">
                    {book.description || 'Sem descrição'}
                  </span>
                </div>
              </Link>
            ))
          )}
        </div>
        <div className="bottom-actions">
          <button className="btn-add-book" onClick={() => setIsModalOpen(true)}>
            Adicionar Livro
          </button>
          <button className={`btn-edit-book ${isEditing ? 'editing-active' : ''}`} onClick={() => setIsEditing(!isEditing)}>
            <img src={PencilIcon} alt="Lápis" className="nav-icon" />
          </button>
        </div>
      </main>

      <HomeAddBookModal 
        isOpen={isModalOpen} 
        onClose={() => setIsModalOpen(false)} 
        onAddBook={handleAddNewBook} 
      />
      
    </div>
  );
};

export default Home;