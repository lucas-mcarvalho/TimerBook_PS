import React from 'react';
import PencilIcon from '../assets/Home/PencilIcon.svg';

const LibraryView = ({ books, isEditing, setIsEditing, onRead, onDelete, onStats, onOpenModal }) => {
  return (
    <>
      <h1>Sua Biblioteca</h1>
      <div className="books-grid">
        {books.length === 0 ? (
          <button className="book-card add-new-card" onClick={onOpenModal} style={{ border: 'none', background: 'transparent' }}>
            <div className="book-cover-wrapper"><div className="book-cover-placeholder">+</div></div>
            <div className="book-info"><h3 className="book-title">Adicionar novo livro</h3></div>
          </button>
        ) : (
          books.map((book) => (
            <div 
              key={book.id} 
              className="book-card"
              onClick={() => !isEditing && onRead(book)}
              style={{ cursor: isEditing ? 'default' : 'pointer' }}
            >
              {isEditing && (
                <button 
                  className="btn-delete-book"
                  onClick={(e) => { 
                    e.stopPropagation(); 
                    onDelete(book.id); 
                  }}
                >
                  X
                </button>
              )}
              
              <div className="book-cover-wrapper">
                {(book.cover || book.coverUrl) && (
                  <img 
                    src={book.cover?.startsWith('blob:') ? book.cover : `http://localhost:8080/${book.coverUrl}`}
                    alt={`Capa de ${book.name}`}
                    className="book-cover-image"
                    onError={(e) => { e.target.style.display = 'none'; }}
                  />
                )}
                <div className="book-cover-placeholder">{book.name ? book.name.charAt(0) : '?'}</div>
              </div>

              <div className="book-info">
                <h3 className="book-title">{book.name}</h3>
                <span className="book-year">{book.description || 'Sem descrição'}</span>
                <button
                  onClick={(e) => {
                    e.stopPropagation(); 
                    onStats(book.id);
                  }}
                  className="btn-stats-trigger"
                  style={{
                    marginTop: '10px',
                    padding: '8px 12px',
                    borderRadius: '8px',
                    border: 'none',
                    background: '#2d89ef',
                    color: '#fff',
                    cursor: 'pointer'
                  }}
                >
                  Ver estatísticas
                </button>
              </div>
            </div>
          ))
        )}
      </div>

      <div className="bottom-actions">
        <button className="btn-add-book" onClick={onOpenModal}>
          Adicionar Livro
        </button>
        <button
          className={`btn-edit-book ${isEditing ? 'editing-active' : ''}`}
          onClick={() => setIsEditing(!isEditing)}
        >
          <img src={PencilIcon} alt="Lápis" className="nav-icon" />
        </button>
      </div>
    </>
  );
};

export default LibraryView;