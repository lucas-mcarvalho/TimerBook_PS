import api from "../axiosApi";

export async function createCheckoutSession({ planId, successUrl, cancelUrl }) {
  try {
    const response = await api.post("/billing/checkout-session", {
      planId,
      successUrl,
      cancelUrl,
    });

    return response.data;
  } catch (error) {
    console.error("Erro ao criar checkout:", error.response?.data || error.message);
    throw error;
  }
}

export async function getMySubscription() {
  try {
    const response = await api.get("/billing/subscription/me");
    return response.data;
  } catch (error) {
    console.error("Erro ao buscar assinatura:", error.response?.data || error.message);
    throw error;
  }
}

export async function createCustomerPortal() {
  try {
    const response = await api.post("/billing/customer-portal");
    return response.data;
  } catch (error) {
    console.error("Erro ao abrir portal do cliente:", error.response?.data || error.message);
    throw error;
  }
}

export async function resendPaymentReceipt() {
  try {
    const response = await api.post("/billing/receipt/resend");
    return response.data;
  } catch (error) {
    console.error("Erro ao reenviar comprovante:", error.response?.data || error.message);
    throw error;
  }
}
