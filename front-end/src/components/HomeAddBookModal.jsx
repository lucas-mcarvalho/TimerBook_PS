import React, { useState, useEffect, useMemo } from 'react';
import '../styles/HomeAddBookModal.css';
import { registerBook } from '../features/books/booksApi.js';
import {getUser} from "../features/user/userApi.js";

const HomeAddBookModal = ({ isOpen, onClose, onAddBook }) => {
  const [newName, setNewName] = useState('');
  const [newDescription, setNewDescription] = useState(''); 
  const [coverFile, setCoverFile] = useState(null);
  const [pdfFile, setPdfFile] = useState(null);
  const [isLoading, setIsLoading] = useState(false);

  const [coverPreviewUrl, setCoverPreviewUrl] = useState(null);
  const [pdfPreviewUrl, setPdfPreviewUrl] = useState(null);

  useEffect(() => {
    if (!coverFile) {
      setCoverPreviewUrl(null);
      return;
    }
    const objectUrl = URL.createObjectURL(coverFile);
    setCoverPreviewUrl(objectUrl);
    return () => URL.revokeObjectURL(objectUrl);
  }, [coverFile]);

  useEffect(() => {
    if (!pdfFile) {
      setPdfPreviewUrl(null);
      return;
    }
    const objectUrl = URL.createObjectURL(pdfFile);
    setPdfPreviewUrl(objectUrl);
    return () => URL.revokeObjectURL(objectUrl);
  }, [pdfFile]);

  const coverViewer = useMemo(() => {
    if (!coverPreviewUrl) return null;
    return (
      <img 
        src={coverPreviewUrl} 
        alt="Capa preview" 
        style={{ width: '100%', height: '100%', objectFit: 'cover', borderRadius: '8px', pointerEvents: 'none' }} 
      />
    );
  }, [coverPreviewUrl]);

  const pdfViewer = useMemo(() => {
  if (!pdfPreviewUrl) return null;
  return (
    <embed 
      src={pdfPreviewUrl}
      type="application/pdf"
      style={{ 
        width: '100%', 
        height: '100%', 
        border: 'none', 
        borderRadius: '8px', 
        pointerEvents: 'none' 
      }} 
    />
  );
}, [pdfPreviewUrl]);  

  if (!isOpen) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!newName.trim()) return;

    setIsLoading(true);

    try {
      const response = await getUser();
      const userId = response.data.id;
      console.log("ID do usuário obtido:", userId);
      console.log("Dados do livro a registrar:", { name: newName, description: newDescription, coverFile, pdfFile });
      const bookData = { name: newName, description: newDescription };
      const savedBookFromServer = await registerBook(
        userId,
        bookData, 
        coverFile || undefined, 
        pdfFile || undefined
      );

      onAddBook({ 
        ...savedBookFromServer, 
        cover: coverPreviewUrl 
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
                <label>Nome</label>
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
                <label>Capa (opcional)</label>
                <div className="upload-box" style={{ position: 'relative' }}>
                  <input 
                    type="file" 
                    accept="image/*" 
                    className="file-input-hidden" 
                    onChange={(e) => setCoverFile(e.target.files?.[0] || null)} 
                  />
                  {coverViewer ? coverViewer : <span>Pré-visualização</span>}
                </div>
              </div>
              
              <div className="upload-section">
                <label>Upload de PDF</label>
                <div className="upload-box" style={{ position: 'relative' }}>
                  <input 
                    type="file" 
                    accept="application/pdf" 
                    className="file-input-hidden" 
                    onChange={(e) => setPdfFile(e.target.files?.[0] || null)} 
                  />
                  {pdfViewer ? pdfViewer : <span>Pré-visualização</span>}
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