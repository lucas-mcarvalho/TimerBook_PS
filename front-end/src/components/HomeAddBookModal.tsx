import React, { useState } from 'react';
import '../styles/HomeAddBookModal.css';

//precisa receber da Home para funcionar
interface HomeAddBookModalProps {
  isOpen: boolean;
  onClose: () => void;
  onAddBook: (newBook: { title: string; author: string; year: string }) => void;
}

const HomeAddBookModal: React.FC<HomeAddBookModalProps> = ({ isOpen, onClose, onAddBook }) => {
  const [newTitle, setNewTitle] = useState('');
  const [newAuthor, setNewAuthor] = useState('');
  const [newYear, setNewYear] = useState('');
  const [coverFile, setCoverFile] = useState<File | null>(null);
  const [pdfFile, setPdfFile] = useState<File | null>(null);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!newTitle.trim() || !newAuthor.trim() || !newYear.trim()) return;

    //estrutura para a API
    const formData = new FormData();
    formData.append('title', newTitle);
    formData.append('author', newAuthor);
    formData.append('year', newYear);
    if (coverFile) formData.append('cover', coverFile);
    if (pdfFile) formData.append('pdf', pdfFile);

    try {
      //chamada API
      //const response = await fetch('http://localhost:8080/api/books', { method: 'POST', body: formData });
      
      //simulação local
      onAddBook({ title: newTitle, author: newAuthor, year: newYear });

      setNewTitle('');
      setNewAuthor('');
      setNewYear('');
      setCoverFile(null);
      setPdfFile(null);
      onClose();
    } catch (error) {
      console.error("Erro ao comunicar com a API:", error);
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
                <label>Qual é o nome?</label>
                <input type="text" value={newTitle} onChange={(e) => setNewTitle(e.target.value)} required />
              </div>
              <div className="form-group">
                <label>Qual é o autor?</label>
                <input type="text" value={newAuthor} onChange={(e) => setNewAuthor(e.target.value)} required />
              </div>
              <div className="form-group">
                <label>Qual é a data de publicação?</label>
                <input type="text" value={newYear} onChange={(e) => setNewYear(e.target.value)} required />
              </div>
              <button type="submit" className="btn-concluir">Concluir</button>
            </div>

            <div className="modal-uploads-col">
              <div className="upload-section">
                <label>Upload de capa</label>
                <div className="upload-box">
                  <input type="file" accept="image/*" className="file-input-hidden" onChange={(e) => setCoverFile(e.target.files?.[0] || null)} />
                  <span>{coverFile ? coverFile.name : "Pré-visualização"}</span>
                </div>
              </div>

              <div className="upload-section">
                <label>Upload de PDF</label>
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