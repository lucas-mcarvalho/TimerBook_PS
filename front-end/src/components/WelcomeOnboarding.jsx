import React, { useState } from "react";
import "../styles/WelcomeOnboarding.css";

const WelcomeOnboarding = ({ onClose }) => {
  const [dailyPages, setDailyPages] = useState("");
  const [monthlyBooks, setMonthlyBooks] = useState("");
  const [dailyMinutes, setDailyMinutes] = useState("");

  const handleSubmit = (e) => {
    e.preventDefault();

    const onboardingData = {
      dailyPages,
      monthlyBooks,
      dailyMinutes,
    };

    console.log("Dados do onboarding:", onboardingData);

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

        <p>
          Antes de começar, queremos conhecer seus objetivos de leitura.
        </p>

        <form onSubmit={handleSubmit} className="onboarding-form">
          <label>Quantas páginas você quer ler por dia?</label>
          <input
            type="number"
            value={dailyPages}
            onChange={(e) => setDailyPages(e.target.value)}
            placeholder="Ex: 20"
            required
          />

          <label>Quantos livros você quer ler por mês?</label>
          <input
            type="number"
            value={monthlyBooks}
            onChange={(e) => setMonthlyBooks(e.target.value)}
            placeholder="Ex: 2"
            required
          />

          <label>Quantos minutos você quer ler por dia?</label>
          <input
            type="number"
            value={dailyMinutes}
            onChange={(e) => setDailyMinutes(e.target.value)}
            placeholder="Ex: 30"
            required
          />

          <button type="submit">Começar</button>
        </form>
      </div>
    </div>
  );
};

export default WelcomeOnboarding;