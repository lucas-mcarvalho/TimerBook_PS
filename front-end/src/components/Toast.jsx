import React, { useState, useCallback } from 'react';
import { ToastContext } from './ToastContext.js';

export const ToastProvider = ({ children }) => {
  const [toasts, setToasts] = useState([]);

  const removeToast = useCallback((id) => {
    setToasts((prev) => 
      prev.map((t) => t.id === id ? { ...t, exiting: true } : t)
    );
    
    setTimeout(() => {
      setToasts((prev) => prev.filter((t) => t.id !== id));
    }, 400);
  }, []);

  const showToast = useCallback((message, type = 'info', options = {}) => {
    const id = `${Date.now()}-${Math.random()}`;
    const duration = options.duration || 4000;
    setToasts((prev) => [...prev, { id, message, type, duration, exiting: false }]);

    setTimeout(() => {
      removeToast(id);
    }, duration);
  }, [removeToast]);

  const showAchievementToast = useCallback((achievement) => {
    const title = achievement?.nome || achievement?.name || "Conquista";
    const description = achievement?.descricao || achievement?.description || "Nova conquista desbloqueada";
    const icon = achievement?.icone || achievement?.icon || "✓";

    showToast({ title, description, icon }, "achievement", { duration: 6200 });
  }, [showToast]);

  const regularToasts = toasts.filter((toast) => toast.type !== "achievement");
  const achievementToasts = toasts.filter((toast) => toast.type === "achievement");

  return (
    <ToastContext.Provider value={{ showToast, showAchievementToast }}>
      {children}
      <div className="toast-container">
        {regularToasts.map((toast) => (
          <div 
            key={toast.id} 
            className={`toast-item toast-${toast.type} ${toast.exiting ? 'exiting' : ''}`}
            onClick={() => removeToast(toast.id)}
          >
            <span className="toast-icon">
              {toast.type === 'success' ? '✓' : toast.type === 'error' ? '✗' : 'ℹ'}
            </span>
            <div className="toast-content">
              {toast.message.split('\n').map((line, i) => (
                <div key={i}>{line}</div>
              ))}
            </div>
          </div>
        ))}
      </div>
      <div className="achievement-toast-container" aria-live="polite">
        {achievementToasts.map((toast) => (
          <button
            type="button"
            key={toast.id}
            className={`achievement-toast ${toast.exiting ? 'exiting' : ''}`}
            style={{ '--toast-duration': `${toast.duration}ms` }}
            onClick={() => removeToast(toast.id)}
          >
            <span className="achievement-toast-glow" />
            <span className="achievement-toast-icon">{toast.message.icon}</span>
            <span className="achievement-toast-copy">
              <span className="achievement-toast-kicker">Conquista desbloqueada</span>
              <span className="achievement-toast-title">{toast.message.title}</span>
              <span className="achievement-toast-description">{toast.message.description}</span>
            </span>
            <span className="achievement-toast-progress" />
          </button>
        ))}
      </div>
    </ToastContext.Provider>
  );
};
