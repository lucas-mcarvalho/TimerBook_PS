import { useEffect, useMemo, useState } from "react";
import Sidebar from "../components/Sidebar";
import { useToast } from "../components/ToastContext.js";
import { getBookByUserId } from "../features/books/booksApi.js";
import {
  createCheckoutSession,
  createCustomerPortal,
  getMySubscription,
  resendPaymentReceipt,
} from "../features/payments/paymentApi.js";
import { getUser } from "../features/user/userApi.js";

import "../styles/Layout.css";
import "../styles/HomeDark.css";
import "../styles/Assinatura.css";

const plans = [
  {
    id: "free",
    name: "Gratuito",
    price: "R$ 0",
    period: "para sempre",
    description: "Para organizar sua biblioteca e manter o ritmo de leitura.",
    features: ["Biblioteca pessoal", "Registro de leituras", "Meta diária", "Perfil do leitor"],
    actionLabel: "Plano atual",
  },
  {
    id: "monthly",
    name: "Premium mensal",
    price: "R$ 19,90",
    period: "por mês",
    description: "Para acompanhar evolução, estatísticas e recursos avançados.",
    badge: "Mais flexível",
    features: ["Livros ilimitados", "Estatísticas completas", "Relatórios de progresso", "Recursos de IA"],
    actionLabel: "Assinar mensal",
  },
];

function getCheckoutUrl(data) {
  return data?.checkoutUrl || data?.paymentUrl || data?.redirectUrl || data?.initPoint || data?.url;
}

function formatSubscriptionStatus(status) {
  const labels = {
    ACTIVE: "Premium ativo",
    FREE: "Gratuito",
    PENDING: "Pagamento pendente",
    CANCELLED: "Cancelado",
    PAST_DUE: "Pagamento atrasado",
  };

  return labels[status] || status || "Gratuito";
}

export default function Assinatura() {
  const { showToast } = useToast();
  const [books, setBooks] = useState([]);
  const [userInfo, setUserInfo] = useState(null);
  const [subscription, setSubscription] = useState(null);
  const [loading, setLoading] = useState(true);
  const [loadingPlanId, setLoadingPlanId] = useState(null);
  const [openingPortal, setOpeningPortal] = useState(false);
  const [resendingReceipt, setResendingReceipt] = useState(false);
  const [selectedPlanId, setSelectedPlanId] = useState("monthly");
  const [statusMessage, setStatusMessage] = useState("");
  const [isDarkMode, setIsDarkMode] = useState(() => {
    const savedTheme = localStorage.getItem("timerbook-theme");
    return savedTheme === "dark";
  });

  useEffect(() => {
    localStorage.setItem("timerbook-theme", isDarkMode ? "dark" : "light");
  }, [isDarkMode]);

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const paymentStatus = params.get("payment");

    if (paymentStatus === "success") {
      setStatusMessage("Pagamento recebido. A assinatura será atualizada assim que o Mercado Pago confirmar o webhook.");
    }

    if (paymentStatus === "cancel") {
      setStatusMessage("Pagamento cancelado. Você pode tentar novamente quando quiser.");
    }
  }, []);

  useEffect(() => {
    async function fetchData() {
      try {
        const [userData, subscriptionData] = await Promise.all([
          getUser(),
          getMySubscription(),
        ]);
        const info = userData.data || userData;
        setUserInfo(info);
        setSubscription(subscriptionData);

        if (info?.id) {
          const booksData = await getBookByUserId(info.id);
          setBooks(booksData);
        }
      } catch (error) {
        console.error("Erro ao carregar dados da assinatura:", error);
        showToast("Não foi possível carregar seus dados de assinatura.", "error");
      } finally {
        setLoading(false);
      }
    }

    fetchData();
  }, [showToast]);

  const selectedPlan = useMemo(
    () => plans.find((plan) => plan.id === selectedPlanId) || plans[1],
    [selectedPlanId]
  );

  const subscriptionStatus = subscription?.status || userInfo?.subscriptionPlan || "FREE";
  const currentPlanName = formatSubscriptionStatus(subscriptionStatus);
  const renewalDate = subscription?.currentPeriodEnd || userInfo?.subscriptionRenewalDate || userInfo?.planRenewalDate;

  const handleSubscribe = async (plan) => {
    setSelectedPlanId(plan.id);
    setStatusMessage("");

    if (plan.id === "free") {
      showToast("Você já está no plano gratuito.", "info");
      return;
    }

    setLoadingPlanId(plan.id);

    try {
      const checkoutData = await createCheckoutSession({
        planId: plan.id,
        successUrl: `${window.location.origin}/assinatura?payment=success`,
        cancelUrl: `${window.location.origin}/assinatura?payment=cancel`,
      });

      const checkoutUrl = getCheckoutUrl(checkoutData);

      if (checkoutUrl) {
        window.location.assign(checkoutUrl);
        return;
      }

      setStatusMessage("O checkout foi criado, mas o servidor não retornou uma URL de pagamento.");
      showToast("Checkout sem URL de redirecionamento.", "error");
    } catch (error) {
      const status = error.response?.status;
      const unavailableCheckout = status === 404 || status === 501 || !error.response;
      const message = unavailableCheckout
        ? "O checkout não está disponível. Verifique a configuração do Mercado Pago no back-end."
        : "Não foi possível iniciar o pagamento. Tente novamente.";

      setStatusMessage(message);
      showToast(message, "error");
    } finally {
      setLoadingPlanId(null);
    }
  };

  const handleOpenPortal = async () => {
    setOpeningPortal(true);

    try {
      const portalData = await createCustomerPortal();
      if (portalData?.url) {
        window.location.assign(portalData.url);
        return;
      }

      showToast("O portal do cliente não retornou uma URL.", "error");
    } catch (error) {
      showToast("Não foi possível abrir o portal da assinatura.", "error");
    } finally {
      setOpeningPortal(false);
    }
  };

  const handleResendReceipt = async () => {
    setResendingReceipt(true);

    try {
      await resendPaymentReceipt();
      showToast("Comprovante reenviado para o e-mail da sua conta.", "success");
    } catch (error) {
      const message = error.response?.status === 404
        ? "Nenhum pagamento aprovado foi encontrado para reenviar."
        : "Não foi possível reenviar o comprovante agora.";
      showToast(message, "error");
    } finally {
      setResendingReceipt(false);
    }
  };

  return (
    <div className={`dashboard-container ${isDarkMode ? "dark-theme" : ""}`}>
      <Sidebar
        menuAtivo="assinatura"
        books={books}
        isDarkMode={isDarkMode}
        setIsDarkMode={setIsDarkMode}
      />

      <main className="main-content subscription-page">
        <div className="subscription-header">
          <div>
            <span className="subscription-kicker">Plano e pagamento</span>
            <h1>Assinatura TimerBook</h1>
            <p>
              Escolha o plano que combina com sua rotina de leitura e continue
              pelo checkout seguro.
            </p>
          </div>

          <div className="subscription-status">
            <span>Plano atual</span>
            <strong>{currentPlanName}</strong>
            {subscription?.provider && <small>Provider: {subscription.provider}</small>}
            {renewalDate && <small>Renova em {new Date(renewalDate).toLocaleDateString("pt-BR")}</small>}
            {subscription?.providerSubscriptionId && (
              <>
                <button
                  type="button"
                  className="subscription-portal-button"
                  onClick={handleOpenPortal}
                  disabled={openingPortal}
                >
                  {openingPortal ? "Abrindo..." : "Gerenciar"}
                </button>
                <button
                  type="button"
                  className="subscription-portal-button"
                  onClick={handleResendReceipt}
                  disabled={resendingReceipt}
                >
                  {resendingReceipt ? "Reenviando..." : "Reenviar comprovante"}
                </button>
              </>
            )}
          </div>
        </div>

        {statusMessage && <div className="subscription-alert">{statusMessage}</div>}

        {loading ? (
          <div className="subscription-loading">Carregando planos...</div>
        ) : (
          <>
            <section className="plans-grid" aria-label="Planos disponíveis">
              {plans.map((plan) => {
                const isSelected = selectedPlanId === plan.id;
                const isLoadingPlan = loadingPlanId === plan.id;

                return (
                  <article
                    key={plan.id}
                    className={`plan-card ${isSelected ? "selected" : ""}`}
                  >
                    <div className="plan-card-top">
                      <div>
                        <h2>{plan.name}</h2>
                        <p>{plan.description}</p>
                      </div>
                      {plan.badge && <span className="plan-badge">{plan.badge}</span>}
                    </div>

                    <div className="plan-price-row">
                      <strong>{plan.price}</strong>
                      <span>{plan.period}</span>
                    </div>

                    <ul className="plan-features">
                      {plan.features.map((feature) => (
                        <li key={feature}>
                          <span className="feature-check">✓</span>
                          {feature}
                        </li>
                      ))}
                    </ul>

                    <button
                      type="button"
                      className={`plan-action ${plan.id === "free" ? "secondary" : ""}`}
                      onClick={() => handleSubscribe(plan)}
                      disabled={loadingPlanId !== null}
                    >
                      {isLoadingPlan ? "Abrindo checkout..." : plan.actionLabel}
                    </button>
                  </article>
                );
              })}
            </section>

            <section className="checkout-summary" aria-label="Resumo do checkout">
              <div>
                <span>Plano selecionado</span>
                <strong>{selectedPlan.name}</strong>
              </div>
              <div>
                <span>Valor</span>
                <strong>{selectedPlan.price}</strong>
              </div>
              <div>
                <span>Pagamento</span>
                <strong>Checkout externo</strong>
              </div>
            </section>
          </>
        )}
      </main>
    </div>
  );
}
