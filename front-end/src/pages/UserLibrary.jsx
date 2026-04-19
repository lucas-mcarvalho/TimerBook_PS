import React, { useState, useEffect } from "react";
import { getBooks, deleteBook } from "../features/books/booksApi.js";
import { useNavigate } from "react-router-dom";
import { endReadingSession, getSessionsByReadingId, startReading } from "../features/books/readSessions.js";
import {getUser} from "../features/user/userApi.js";

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
          onClick={(e) => { 
            e.preventDefault(); 
            e.stopPropagation();
            if (window.confirm(`Deseja realmente deletar "${book.name}"?`)) {
              onDelete(book.id);
            }
          }}
        >
          X
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
          style={{ marginTop: 10, width: '100%', background: '#2d89ef', color: '#fff', padding: '8px 0', borderRadius: 6, fontSize: 14, border: 'none', cursor: 'pointer' }}
        >
          Ver estatísticas
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

  const loadBooks = async () => {
    try {
      setLoading(true);
      setError(null);
      const booksData = await getBooks();
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
      //Melhorar esse trecho, apenas
      const response = await getUser();
      const userId = response.data.id;


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
      console.error("Erro ao iniciar leitura:", err);
      setError("Erro ao iniciar leitura: " + err.message);
    }
  };
   const handleOpenStats = async (bookId) => {
    try {
      const response = await fetch(`http://localhost:8080/readings/book/${bookId}`);
      if (!response.ok) {
        throw new Error("Não foi possível buscar as leituras do livro.");
      }
      const readings = await response.json();
      if (!readings || readings.length === 0) {
        alert("Esse livro ainda não possui leituras registradas.");
        return;
      }
      // pega a leitura mais recente
      const latestReading = readings[readings.length - 1];
      // Usa navigate do escopo UserLibrary
      navigate(`/estatisticas/${latestReading.id}`);
    } catch (error) {
      console.error("Erro ao abrir estatísticas:", error);
      alert("Erro ao abrir estatísticas.");
    }
  };

  const handleDelete = async (bookId) => {
    try {
      await deleteBook(bookId);
      setBooks((prev) => prev.filter((b) => b.id !== bookId));
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