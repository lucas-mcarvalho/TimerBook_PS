import React, { useState, useEffect } from "react";
import { deleteBook } from "../features/books/booksApi.js";
import { getUser } from "../features/user/userApi.js";
import { getBookByUserId } from "../features/books/booksApi.js";
import { useNavigate } from "react-router-dom";
import { endReading, getReadingInProgressByBookId, startBookReadingSession } from "../features/books/readSessions.js";

import '../styles/Layout.css';
import '../styles/Library.css';
import '../styles/Home.css';
import '../styles/HomeDark.css'; 
import PencilIcon from '../assets/Home/PencilIcon.svg';
import Sidebar from '../components/Sidebar';
import HomeAddBookModal from '../components/HomeAddBookModal';

function BookCard({ book, onRead, onDelete, isEditing, onOpenStats }) {
  return (
    <div 
      className="book-card" 
      onClick={() => !isEditing && onRead(book)} 
      style={{ cursor: isEditing ? 'default' : 'pointer' }}
    >
      {isEditing && (
        <button 
          className="btn-delete-book"
          title="Excluir livro"
          onClick={(e) => { 
            e.preventDefault(); 
            e.stopPropagation();
            if (window.confirm(`Deseja realmente deletar "${book.name}"?`)) {
              onDelete(book.id);
            }
          }}
        >
          <svg 
            xmlns="http://www.w3.org/2000/svg" 
            width="18" 
            height="18" 
            viewBox="0 0 24 24" 
            fill="none" 
            stroke="currentColor" 
            strokeWidth="2" 
            strokeLinecap="round" 
            strokeLinejoin="round"
          >
            <path d="M3 6h18"></path>
            <path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6"></path>
            <path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"></path>
          </svg>
        </button>
      )}

      <div className="book-cover-wrapper">
        {(book.coverUrl) && (
          <img 
            src={`http://localhost:8080/${book.coverUrl}`} 
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
        <button
          onClick={(e) => { e.stopPropagation(); onOpenStats(book.id); }}
          className="btn-stats"
        >
          Ver estatísticas
        </button>
        <button
          onClick={async (e) => {
              e.stopPropagation();
              const response = await getUser();
              const userId = response.data.id;
              const reading = await getReadingInProgressByBookId(book.id);

              if (!reading?.id) {
                alert("Esse livro ainda não possui leitura em andamento.");
                return;
              }

              const finalPage = Number(window.prompt("Página final da leitura:", reading.currentPage || 1));
              if (!Number.isFinite(finalPage) || finalPage < 1) return;

              console.log("id da leitura:", reading.id);
              console.log("id do usuário:", userId);
              
              await endReading(reading.id, userId, {
                  finalPage
              });
          }}
          className="btn-stats"
        >
          Finalizar Leitura
        </button>
      </div>
    </div>
  );
}

function UserLibrary() {
  const [books, setBooks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [isDarkMode, setIsDarkMode] = useState(() => {
    const savedTheme = localStorage.getItem('timerbook-theme');
    return savedTheme === 'dark';
  });
  
  const navigate = useNavigate();

  useEffect(() => {
    localStorage.setItem('timerbook-theme', isDarkMode ? 'dark' : 'light');
  }, [isDarkMode]);

  useEffect(() => {
    loadBooks();
  }, []);

  useEffect(() => {
    if (!isEditing) return;

    const handlePageButtonClick = (event) => {
      const clickedButton = event.target?.closest?.("button");

      if (!clickedButton) return;

      const shouldKeepEditing =
        clickedButton.closest(".btn-edit-book") ||
        clickedButton.closest(".btn-delete-book");

      if (!shouldKeepEditing) {
        setIsEditing(false);
      }
    };

    document.addEventListener("click", handlePageButtonClick, true);
    return () => document.removeEventListener("click", handlePageButtonClick, true);
  }, [isEditing]);

  const loadBooks = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await getUser();
      const userId = response.data.id;
      const booksData = await getBookByUserId(userId);
      setBooks(booksData);
    } catch (err) {
      console.error("Erro ao carregar livros:", err);
      setError("Erro ao carregar livros: " + err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleAddNewBook = (serverBook) => {
    setBooks(prev => [...prev, serverBook]);
  };

  const handleRead = async (book) => {
    try {
      const response = await getUser();
      const userId = response.data.id;
      const { sessionId, initialPage } = await startBookReadingSession(userId, book);

      navigate("/leitor", { state: { book, sessionId, initialPage } });
    } catch (err) {
      console.error("Erro ao iniciar leitura:", err);
      setError("Erro ao iniciar leitura: " + err.message);
    }
  };
  
  const handleOpenStats = async (readingId) => {
     navigate(`/estatisticas/${readingId}`);
  };

  const handleDelete = async (bookId) => {
    try {
      await deleteBook(bookId);
      setBooks((prev) => prev.filter((b) => b.id !== bookId));
      setIsEditing(false);
    } catch (err) {
      console.error("Erro ao deletar livro:", err);
      setError("Erro ao deletar livro: " + err.message);
    }
  };

  return (
    <div className={`dashboard-container ${isDarkMode ? 'dark-theme' : ''}`}>
      
      <Sidebar 
        menuAtivo="livros" 
        books={books} 
        isDarkMode={isDarkMode} 
        setIsDarkMode={setIsDarkMode} 
      />

      <main className="main-content">
        <h1>Minha Biblioteca</h1>
        
        {loading ? (
          <p>Carregando seus livros...</p>
        ) : error ? (
          <p style={{ color: 'red' }}>{error}</p>
        ) : (
          <div className="books-grid">
            {books.length === 0 ? (
              <button className="book-card add-new-card" onClick={() => setIsModalOpen(true)} style={{border: 'none', background: 'transparent'}}>
                <div className="book-cover-wrapper"><div className="book-cover-placeholder">+</div></div>
                <div className="book-info"><h3 className="book-title">Adicionar novo livro</h3></div>
              </button>
            ) : (
              books.map((book) => (
                <BookCard 
                  key={book.id} 
                  book={book} 
                  onRead={handleRead} 
                  onDelete={handleDelete} 
                  isEditing={isEditing}
                  onOpenStats={handleOpenStats}
                />
              ))
            )}
          </div>
        )}
        
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
}

export default UserLibrary;