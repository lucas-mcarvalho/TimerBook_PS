import React, { useState } from "react";
import { updateReadingGoal } from "../features/user/userApi.js";
import "../styles/WelcomeOnboarding.css";

const WelcomeOnboarding = ({ onClose }) => {
  const [readingGoal, setReadingGoal] = useState("");
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!readingGoal || isSaving) return;

    setIsSaving(true);
    setError("");

    try {
      await updateReadingGoal(readingGoal);
      onClose();
    } catch (err) {
      console.error("Erro ao salvar meta de leitura:", err);
      setError("Não foi possível salvar sua meta. Tente novamente.");
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <div className="onboarding-overlay">
      <div className="onboarding-card">
        <div className="book-animation">
          <div className="book">
            <div className="page page1"></div>
            <div className="page page2"></div>
            <div className="page page3"></div>
          </div>
        </div>

        <h1>Bem-vindo ao TimerBook!</h1>

        <p>Escolha sua meta diária de leitura para começar.</p>

        <form onSubmit={handleSubmit} className="onboarding-form">
          <div className="goal-options">
            {[10, 20, 30].map((minutes) => (
              <button
                key={minutes}
                type="button"
                className={`goal-option ${
                  readingGoal === minutes ? "selected" : ""
                }`}
                onClick={() => setReadingGoal(minutes)}
                disabled={isSaving}
              >
                <strong>{minutes}</strong>
                <span>minutos por dia</span>
              </button>
            ))}
          </div>

          {error && <p className="onboarding-error">{error}</p>}

          <button type="submit" disabled={!readingGoal || isSaving}>
            {isSaving ? "Salvando..." : "Começar"}
          </button>
        </form>
      </div>
    </div>
  );
};

export default WelcomeOnboarding;
