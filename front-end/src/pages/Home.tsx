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

export interface Book {
  id: number;
  name: string;
  description: string;
  cover?: string; 
  coverUrl?: string;
  dataPath?: string;
}

const Home: React.FC = () => {
  const [menuAtivo, setMenuAtivo] = useState('livros');
  const [books, setBooks] = useState<Book[]>([]);

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  
  const [isDarkMode, setIsDarkMode] = useState<boolean>(() => {
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

  const handleDeleteBook = async (idToRemove: number) => {
    try {
      await deleteBook(idToRemove);
      setBooks(prev => prev.filter(book => book.id !== idToRemove));
    } catch (error) {
      console.error("Erro ao apagar o livro:", error);
      alert("Ops! Não consegui apagar o livro. O servidor pode estar fora do ar.");
    }
  };

  const handleAddNewBook = (serverBook: Book) => {
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
          <a href="#" className={`nav-item ${menuAtivo === 'inicio' ? 'active' : ''}`} onClick={(e) => { e.preventDefault(); setMenuAtivo('inicio'); }}>
            <img src={homeIcon} alt="Início" className="nav-icon" /> Início
          </a>
          <a href="#" className={`nav-item ${menuAtivo === 'livros' ? 'active' : ''}`} onClick={(e) => { e.preventDefault(); setMenuAtivo('livros'); }}>
            <img src={BookIcon} alt="Livros" className="nav-icon" /> Livros
          </a>

          {books.length === 0 ? (
            <div className="empty-books-msg">Sem livros cadastrados</div>
          ) : (
            <div className="sidebar-shortcuts">
              {books.slice(0, 5).map((book) => (
                <Link to="/leitor" state={{ filePath: book.dataPath }} key={book.id} className="sidebar-shortcut-item">
                  {book.name}
                </Link>
              ))}
            </div>
          )}

          <a href="#" className={`nav-item profile-item ${menuAtivo === 'perfil' ? 'active' : ''}`} onClick={(e) => { e.preventDefault(); setMenuAtivo('perfil'); }}>
            <img src={ProfileIcon} alt="Perfil" className="nav-icon" /> Perfil
          </a>
        </nav>
        <div className="sidebar-footer">
          <a href="#" className="action-icon-btn"><img src={ConfigIcon} alt="Configurações" className="nav-icon" /></a>
          
          <a href="#" className="action-icon-btn" onClick={(e) => { e.preventDefault(); setIsDarkMode(!isDarkMode); }}>
            <img 
              src={isDarkMode ? SunIcon : MoonIcon} 
              alt="Aparência" 
              className="nav-icon" 
            />
          </a>
        </div>
      </aside>

      <main className="main-content">
        <h1>Sua Biblioteca</h1>
        <div className="books-grid">
          {books.length === 0 ? (
            <a href="#" className="book-card add-new-card" onClick={(e) => { e.preventDefault(); setIsModalOpen(true); }}>
              <div className="book-cover-wrapper"><div className="book-cover-placeholder">+</div></div>
              <div className="book-info"><h3 className="book-title">Adicionar novo livro</h3></div>
            </a>
          ) : (
            books.map((book) => (
              <Link to="/leitor" state={{ filePath: book.dataPath }} key={book.id} className="book-card">
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
                        (e.target as HTMLImageElement).style.display = 'none';
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
          <a href="#" className="btn-add-book" onClick={(e) => { e.preventDefault(); setIsModalOpen(true); }}>Adicionar Livro</a>
          <a href="#" className={`btn-edit-book ${isEditing ? 'editing-active' : ''}`} onClick={(e) => { e.preventDefault(); setIsEditing(!isEditing); }}>
            <img src={PencilIcon} alt="Lápis" className="nav-icon" />
          </a>
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