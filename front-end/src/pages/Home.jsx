import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom'; 
import '../styles/Home.css';
import '../styles/Sidebar.css';
import '../styles/Welcome.css';
import '../styles/Library.css';
import '../styles/HomeDark.css'; 
import '../styles/Waves.css'; 

import Sidebar from '../components/Sidebar';
import WelcomeView from '../components/WelcomeView';
import LibraryView from '../components/LibraryView';
import HomeAddBookModal from '../components/HomeAddBookModal';

import { getBooks, deleteBook } from '../features/books/booksApi.js';
import { getSessionsByReadingId, startReading } from "../features/books/readSessions.js";

const Home = () => {
  const [menuAtivo, setMenuAtivo] = useState('inicio');
  const [books, setBooks] = useState([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const navigate = useNavigate();
  
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

  const handleRead = async (book) => {
    try {
      const readingResponse = await startReading(book.id);
      const readingId = readingResponse.id;
      const sessions = await getSessionsByReadingId(readingId);
      const sortedSessions = [...sessions].sort((a, b) => new Date(b.startedAt) - new Date(a.startedAt));
      const lastSession = sortedSessions[1]; 
      const currentSession = sortedSessions[0]; 

      let startPage = 1;
      if (lastSession && lastSession.endPage) {
        startPage = lastSession.endPage;
      }
      
      const urlDoPdf = book.pdfUrl || book.dataPath ? `http://localhost:8080/${book.pdfUrl || book.dataPath}` : null;

      navigate("/leitor", { 
        state: { 
            book: { ...book, pdfUrlCompleta: urlDoPdf }, 
            sessionId: currentSession?.id, 
            initialPage: startPage 
        } 
      });
    } catch (err) {
      console.error("Erro ao iniciar sessão de leitura:", err);
      alert("Ops! Erro ao iniciar sessão de leitura: " + err.message);
    }
  };

  const handleDeleteBook = async (idToRemove) => {
    try {
      await deleteBook(idToRemove);
      setBooks(prev => prev.filter(book => book.id !== idToRemove));
    } catch (error) {
      console.error("Erro ao apagar o livro:", error);
      alert("Ops! Não consegui apagar o livro.");
    }
  };

  const handleAddNewBook = (serverBook) => {
    setBooks(prev => [...prev, serverBook]);
  };

  const handleOpenStats = async (bookId) => {
    try {
      const response = await fetch(`http://localhost:8080/readings/book/${bookId}`);
      if (!response.ok) throw new Error("Erro na API");
      const readings = await response.json();
      if (!readings || readings.length === 0) {
        alert("Esse livro ainda não possui leituras registradas.");
        return;
      }
      
      const latestReading = readings[readings.length - 1];
      navigate(`/estatisticas/${latestReading.id}`);
      
    } catch (error) {
      alert("Erro ao abrir estatísticas.");
    }
  };

  return (
    <div className={`dashboard-container ${isDarkMode ? 'dark-theme' : ''}`}>
      <Sidebar 
        menuAtivo={menuAtivo}
        setMenuAtivo={setMenuAtivo}
        books={books}
        onRead={handleRead}
        isDarkMode={isDarkMode}
        setIsDarkMode={setIsDarkMode}
      />

      <main className={`main-content ${menuAtivo === 'inicio' ? 'welcome-mode' : ''}`}>
        {menuAtivo === 'inicio' ? (
          <WelcomeView onGoToLibrary={() => setMenuAtivo('livros')} />
        ) : menuAtivo === 'livros' ? (
          <LibraryView 
            books={books}
            isEditing={isEditing}
            setIsEditing={setIsEditing}
            onRead={handleRead}
            onDelete={handleDeleteBook}
            onStats={handleOpenStats}
            onOpenModal={() => setIsModalOpen(true)}
          />
        ) : (
          <>
            <h1>Perfil do Usuário</h1>
            <p>Configurações da conta em breve.</p>
          </>
        )}
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