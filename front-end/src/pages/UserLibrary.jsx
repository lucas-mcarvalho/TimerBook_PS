import { useState, useEffect } from "react";
import { getBooks, deleteBook } from "../features/books/booksApi.js";
import { useNavigate } from "react-router-dom";
import { endReadingSession, getSessionsByReadingId, startReading } from "../features/books/readSessions.js";


// Componente para cada livro
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

// Página principal da biblioteca do usuário
function UserLibrary() {
  const [books, setBooks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  // Carregar livros ao montar a página
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
    console.log("Lendo livro:", book);
    try { 
      const response = await startReading(book.id, 10);

      console.log("Leitura iniciada:", response);
      console.log("Sessão de leitura iniciada para livro ID:", book.id);
      let lastSession = (await getSessionsByReadingId(response.id))[0]; 
      console.log("Sessão de leitura atual:", lastSession);
  

     await endReadingSession(lastSession.id, 50); // Finaliza a sessão na página 50 (exemplo)

      // Pequeno delay para garantir que o log apareça antes do redirecionamento
      setTimeout(() => {
        navigate("/leitor", { state: { book } });
      }, 100);
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