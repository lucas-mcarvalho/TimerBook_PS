import { useState, useEffect } from "react";
import { getBooks } from "../features/books/booksApi";

function BookList() {
  const [books, setBooks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

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
      setError("Erro ao carregar livros: " + err.message);
      console.error("Erro:", err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <div className="loading">Carregando livros...</div>;
  }

  if (error) {
    return <div className="error">{error}</div>;
  }

  return (
    <div className="book-list-container">
      <h1>Meus Livros</h1>
      
      {books.length === 0 ? (
        <div className="empty-state">
          <p>Nenhum livro cadastrado ainda.</p>
        </div>
      ) : (
        <div className="books-grid">
          {books.map((book) => (
            <div key={book.id} className="book-card">
              {book.coverUrl && (
                <div className="book-cover">
                  <img 
                    src={`http://localhost:8080/files/${book.coverUrl}`} 
                    alt={book.name}
                  />
                </div>
              )}
              <div className="book-info">
                <h2 className="book-title">{book.name}</h2>
                <p className="book-description">{book.description}</p>
                <div className="book-actions">
                  <button className="btn-read">Ler</button>
                  <button className="btn-delete">Deletar</button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default BookList;