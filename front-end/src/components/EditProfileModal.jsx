import React, { useState, useEffect } from 'react';
import { updateProfile } from '../features/user/userApi.js';
import '../styles/HomeAddBookModal.css'; 
import '../styles/EditProfileModal.css'; 

export default function EditProfileModal({ isOpen, onClose, userInfo, onUpdateSuccess }) {
  const [formData, setFormData] = useState({ username: "" });
  const [photoFile, setPhotoFile] = useState(null);
  const [photoPreview, setPhotoPreview] = useState(null);
  const [removePhotoFlag, setRemovePhotoFlag] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (userInfo && isOpen) {
      setFormData({
        username: userInfo.username || ""
      });
      const photoPath = userInfo.photopath || userInfo.photo;
      setPhotoPreview(photoPath ? `http://localhost:8080/${photoPath}?t=${Date.now()}` : null);
      setPhotoFile(null);
      setRemovePhotoFlag(false);
      setError(null);
    }
  }, [userInfo, isOpen]);

  if (!isOpen) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (loading) return;

    const userId = userInfo?.id;

    if (!userId) {
      setError("ID do usuário não identificado.");
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const profileData = {
        ...formData,
        email: userInfo.email
      };

      const response = await updateProfile(userId, profileData, photoFile, removePhotoFlag);
      
      onUpdateSuccess({ 
        ...profileData, 
        photopath: (removePhotoFlag && !photoFile) ? null : (response.photopath || userInfo.photopath),
        photo: (removePhotoFlag && !photoFile) ? null : (response.photopath || userInfo.photopath)
      }); 
      
      onClose(); 
    } catch (err) {
      const status = err.response?.status;
      const message = err.response?.data;

      if (status === 400 && typeof message === 'string') {
        const lowerMessage = message.toLowerCase();
        if (lowerMessage.includes("username já está em uso") || lowerMessage.includes("username ja esta em uso")) {
          setError("Este nome de usuário já existe. Escolha outro.");
        } else if (lowerMessage.includes("email já cadastrado") || lowerMessage.includes("email ja cadastrado")) {
          setError("Este e-mail já está em uso.");
        } else {
          setError(message);
        }
      } else {
        setError("Erro ao salvar: Nome de usuário em uso.");
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content-large" onClick={(e) => e.stopPropagation()} style={{ position: 'relative' }}>
        <button className="profile-modal-close-btn" onClick={onClose}>&times;</button>
        <h2 className="modal-title">Editar Perfil</h2>

        {error && <div className="status-message status-error">✗ {error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="modal-body-flex">
            <div className="modal-inputs-col">
              <div className="form-group">
                <label>Nome de Usuário</label>
                <input 
                  type="text" 
                  value={formData.username} 
                  onChange={(e) => setFormData({...formData, username: e.target.value})} 
                  required 
                />
              </div>

              <button type="submit" className="btn-concluir" disabled={loading}>
                {loading ? "Salvando..." : "Concluir"}
              </button>
            </div>

            <div className="modal-uploads-col profile-upload-col">
              <div className="upload-section">
                <label>Foto de Perfil</label>
                <div className="upload-box profile-upload-box" style={{ cursor: 'pointer', position: 'relative' }}>
                  <input 
                    type="file" 
                    className="file-input-hidden" 
                    accept="image/*"
                    onChange={(e) => {
                      const file = e.target.files[0];
                      if (file) {
                        setPhotoFile(file);
                        setPhotoPreview(URL.createObjectURL(file));
                        setRemovePhotoFlag(false);
                      }
                    }} 
                  />
                  {photoPreview ? (
                    <>
                      <img 
                        src={photoPreview} 
                        alt="Preview" 
                        style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                        onError={(e) => {
                          e.target.onerror = null;
                          setPhotoPreview(null);
                        }}
                      />
                      <button
                        type="button"
                        className="btn-remove-photo"
                        onClick={(e) => {
                          e.preventDefault();
                          e.stopPropagation();
                          setPhotoFile(null);
                          setPhotoPreview(null);
                          setRemovePhotoFlag(true);
                        }}
                      >
                        &times;
                      </button>
                    </>
                  ) : (
                    <span>Upload</span>
                  )}
                </div>
              </div>
            </div>
          </div>
        </form>
      </div>
    </div>
  );
}