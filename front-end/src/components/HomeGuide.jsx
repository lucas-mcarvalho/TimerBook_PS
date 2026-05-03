import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import "../styles/HomeGuide.css";

const guideSteps = [
  {
    tag: "MENU",
    title: "Menu lateral",
    text: "Aqui ficam os atalhos principais do sistema.",
    target: "guide-sidebar",
  },
  {
    tag: "INÍCIO",
    title: "Botão Início",
    text: "Esse botão leva você para a tela inicial.",
    target: "guide-menu-home",
  },
  {
    tag: "BIBLIOTECA",
    title: "Botão Biblioteca",
    text: "Aqui você acessa seus livros.",
    target: "guide-menu-library",
  },
  {
    tag: "PERFIL",
    title: "Perfil",
    text: "Acesse seu perfil para ver e editar suas informações.",
    target: "guide-menu-profile",
  },
  {
    tag: "META",
    title: "Meta de leitura",
    text: "No Perfil, use Alterar Meta de Leitura para mudar seus minutos diários.",
    target: "guide-menu-profile",
    actionLabel: "Ir para Perfil",
    actionTo: "/perfil",
  },
  {
    tag: "CONFIGURAÇÕES",
    title: "Configurações",
    text: "Aqui você pode ajustar preferências da sua conta.",
    target: "guide-menu-settings",
  },
  {
    tag: "TEMA",
    title: "Modo noturno",
    text: "O ícone da lua ativa ou desativa o modo escuro.",
    target: "guide-menu-darkmode",
  },
  {
    tag: "PERFIL",
    title: "Seu perfil na Home",
    text: "Aqui aparece sua foto e seu nome.",
    target: "guide-profile",
  },
  {
    tag: "BIBLIOTECA",
    title: "Acessar Biblioteca",
    text: "Clique aqui para ver seus livros.",
    target: "guide-library-button",
  },
];

const HomeGuide = () => {
  const navigate = useNavigate();
  const [isOpen, setIsOpen] = useState(false);
  const [step, setStep] = useState(0);
  const [highlightStyle, setHighlightStyle] = useState(null);

  const currentStep = guideSteps[step];
  const progress = ((step + 1) / guideSteps.length) * 100;

  // 🔥 CALCULA POSIÇÃO DO HIGHLIGHT
  useEffect(() => {
    if (!isOpen) return;

    const frame = requestAnimationFrame(() => {
      const targetId = guideSteps[step].target;
      const element = document.getElementById(targetId);

      if (!element) {
        setHighlightStyle(null);
        return;
      }

      const rect = element.getBoundingClientRect();

      setHighlightStyle({
        top: rect.top - 8,
        left: rect.left - 8,
        width: rect.width + 16,
        height: rect.height + 16,
      });
    });

    return () => cancelAnimationFrame(frame);
  }, [step, isOpen]);

  const nextStep = () => {
    if (step < guideSteps.length - 1) {
      setStep(step + 1);
    } else {
      setIsOpen(false);
      setStep(0);
    }
  };

  const previousStep = () => {
    if (step > 0) {
      setStep(step - 1);
    }
  };

  const closeGuide = () => {
    setIsOpen(false);
    setStep(0);
  };

  const handleStepAction = () => {
    if (!currentStep.actionTo) return;
    closeGuide();
    navigate(currentStep.actionTo);
  };

  return (
    <>
      <button className="guide-button" onClick={() => setIsOpen(true)}>
        Guia
      </button>

      {isOpen && (
        <>
          {/* 🔥 FUNDO ESCURO */}
          <div className="guide-dark-layer"></div>

          {/* 🔥 DESTAQUE */}
          {highlightStyle && (
            <div
              className="guide-highlight"
              style={highlightStyle}
            />
          )}

          {/* CARD */}
          <div className="guide-overlay">
            <div className="guide-card">
              <button className="guide-close" onClick={closeGuide}>
                X
              </button>

              <div className="guide-icon">📘</div>

              <p className="guide-tag">{currentStep.tag}</p>

              <h2>{currentStep.title}</h2>

              <p className="guide-text">{currentStep.text}</p>

              <div className="guide-progress">
                <div
                  className="guide-progress-fill"
                  style={{ width: `${progress}%` }}
                />
              </div>

              <div className="guide-footer">
                <span>
                  {step + 1} de {guideSteps.length}
                </span>

                <div className="guide-actions">
                  {currentStep.actionTo && (
                    <button className="guide-step-action" onClick={handleStepAction}>
                      {currentStep.actionLabel}
                    </button>
                  )}

                  <button onClick={closeGuide}>Pular</button>

                  <button onClick={previousStep} disabled={step === 0}>
                    Voltar
                  </button>

                  <button className="guide-next" onClick={nextStep}>
                    {step === guideSteps.length - 1
                      ? "Finalizar"
                      : "Próximo"}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </>
      )}
    </>
  );
};

export default HomeGuide;
