import React, { useState } from "react";
import "../styles/WelcomeOnboarding.css";

const WelcomeOnboarding = ({ onClose }) => {
  const [readingGoal, setReadingGoal] = useState("");

  const handleSubmit = (e) => {
    e.preventDefault();

    const onboardingData = {
      readingGoalMinutes: readingGoal,
    };

    console.log("Meta escolhida:", onboardingData);

    onClose();
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
              >
                <strong>{minutes}</strong>
                <span>minutos por dia</span>
              </button>
            ))}
          </div>

          <button type="submit" disabled={!readingGoal}>
            Começar
          </button>
        </form>
      </div>
    </div>
  );
};

export default WelcomeOnboarding;