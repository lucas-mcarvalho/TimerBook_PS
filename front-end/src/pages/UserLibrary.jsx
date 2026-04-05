import { useState, useEffect } from "react";
import { getBooks, deleteBook } from "../features/books/booksApi.js";
import { useNavigate } from "react-router-dom";
import { endReadingSession, getSessionsByReadingId, startReading } from "../features/books/readSessions.js";

function BookCard({ book, onRead, onDelete }) {
  return (
    <div className="book-card" style={{ border: "1px solid #ccc", padding: 10, width: 200 }}>
      {book.coverUrl && (
        <div className="book-cover">
          <img 
            src={`http://localhost:8080/${book.coverUrl}`} 
            alt={book.name} 
            style={{ width: "100%", height: "auto" }}
          />
        </div>
      )}
      <div className="book-info">
        <h2 className="book-title">{book.name}</h2>
        <p className="book-description">{book.description}</p>
        <div className="book-actions" style={{ marginTop: 10 }}>
          <button onClick={() => onRead(book)}>Ler</button>
          <button onClick={() => {
            if (window.confirm(`Deseja realmente deletar "${book.name}"?`)) {
              onDelete(book.id);
            }
          }} style={{ marginLeft: 5 }}>
            Deletar
          </button>
        </div>
      </div>
    </div>
  );
}

function UserLibrary() {
  const [books, setBooks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

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

  const handleRead = async (book) => {
    try {
      const readingResponse = await startReading(book.id);
      const readingId = readingResponse.id;
      
      const sessions = await getSessionsByReadingId(readingId);
      
      const sortedSessions = [...sessions].sort((a, b) => new Date(b.startedAt) - new Date(a.startedAt));
      const lastSession = sortedSessions[1];
      const currentSession = sortedSessions[0];
      
      let startPage = 1;
      if (lastSession) {
        startPage = lastSession.endPage;
      }
      
      const urlDoPdf = book.pdfUrl ? `http://localhost:8080/${book.pdfUrl}` : null;
      
      navigate("/leitor", { 
        state: { 
          book: { ...book, pdfUrlCompleta: urlDoPdf },
          sessionId: currentSession?.id, 
          initialPage: startPage 
        } 
      });
      
    } catch (err) {
      console.error("Erro ao iniciar leitura:", err);
      setError("Erro ao iniciar leitura: " + err.message);
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

  if (loading) return <p>Carregando livros...</p>;
  if (error) return <p>{error}</p>;

  return (
    <div className="user-library">
      <h1>Minha Biblioteca</h1>

      {books.length === 0 ? (
        <p>Nenhum livro cadastrado ainda.</p>
      ) : (
        <div className="books-grid" style={{ display: "flex", flexWrap: "wrap", gap: 20 }}>
          {books.map((book) => (
            <BookCard 
              key={book.id} 
              book={book} 
              onRead={handleRead} 
              onDelete={handleDelete} 
            />
          ))}
        </div>
      )}
    </div>
  );
}

export default UserLibrary;