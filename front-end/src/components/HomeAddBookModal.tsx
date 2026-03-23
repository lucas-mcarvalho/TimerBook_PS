import React, { useState } from 'react';
import '../styles/HomeAddBookModal.css';
import { registerBook } from '../features/books/booksApi.js';
import { Book } from '../pages/Home';

interface HomeAddBookModalProps {
  isOpen: boolean;
  onClose: () => void;
  onAddBook: (newBook: Book) => void;
}

const HomeAddBookModal: React.FC<HomeAddBookModalProps> = ({ isOpen, onClose, onAddBook }) => {
  const [newName, setNewName] = useState('');
  const [newDescription, setNewDescription] = useState(''); 
  const [coverFile, setCoverFile] = useState<File | null>(null);
  const [pdfFile, setPdfFile] = useState<File | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newName.trim()) return;

    setIsLoading(true);

    try {
      const bookData = { name: newName, description: newDescription };
      const savedBookFromServer = await registerBook(
        bookData, 
        coverFile || undefined, 
        pdfFile || undefined
      );

      const coverPreview = coverFile ? URL.createObjectURL(coverFile) : undefined;

      onAddBook({ 
        ...savedBookFromServer, 
        cover: coverPreview 
      });

      setNewName(''); setNewDescription(''); setCoverFile(null); setPdfFile(null);
      onClose();
      
    } catch (error) {
      console.error("Erro ao salvar:", error);
      alert("Ops! Falha ao conectar com o servidor.");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content-large" onClick={(e) => e.stopPropagation()}>
        <h2 className="modal-title">Adição de novo livro</h2>
        <form onSubmit={handleSubmit}>
          <div className="modal-body-flex">
            <div className="modal-inputs-col">
              <div className="form-group">
                <label>Nome *</label>
                <input type="text" value={newName} onChange={(e) => setNewName(e.target.value)} required placeholder="Ex: O Pequeno Príncipe" />
              </div>
              <div className="form-group">
                <label>Descrição</label>
                <textarea value={newDescription} onChange={(e) => setNewDescription(e.target.value)} rows={4} placeholder="Digite um breve resumo do livro..." />
              </div>
              <button type="submit" className="btn-concluir" disabled={isLoading}>
                {isLoading ? 'Salvando...' : 'Concluir'}
              </button>
            </div>
            <div className="modal-uploads-col">
              <div className="upload-section">
                <label>Upload de capa (opcional)</label>
                <div className="upload-box">
                  <input type="file" accept="image/*" className="file-input-hidden" onChange={(e) => setCoverFile(e.target.files?.[0] || null)} />
                  <span>{coverFile ? coverFile.name : "Pré-visualização"}</span>
                </div>
              </div>
              <div className="upload-section">
                <label>Upload de PDF (opcional)</label>
                <div className="upload-box">
                  <input type="file" accept="application/pdf" className="file-input-hidden" onChange={(e) => setPdfFile(e.target.files?.[0] || null)} />
                  <span>{pdfFile ? pdfFile.name : "Pré-visualização"}</span>
                </div>
              </div>
            </div>
          </div>
        </form>
      </div>
    </div>
  );
};

export default HomeAddBookModal;