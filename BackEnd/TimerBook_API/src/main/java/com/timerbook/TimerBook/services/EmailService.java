package com.timerbook.TimerBook.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.timerbook.TimerBook.config.EmailConfig;
import com.timerbook.TimerBook.dto.EmailRequestDTO;
import com.timerbook.TimerBook.mail.EmailSender;

import jakarta.mail.MessagingException;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class EmailService {
    @Autowired
    private EmailSender emailSender;

    @Autowired
    private EmailConfig emailConfig;

    public void send(EmailRequestDTO emailRequest) {
        try {
            emailSender.to(emailRequest.getTo())
                    .withSubject(emailRequest.getSubject())
                    .withMessage(emailRequest.getMessage())
                    .send(emailConfig);
        } catch (MessagingException e) {
            throw new IllegalStateException("Failed to send e-mail", e);
        }
    }


    public void sendVerificationEmail(String toEmail, String link) {
        String subject = "TimerBook - Confirme sua conta";
        String message = "Olá!\n\n" +
                "Bem-vindo ao TimerBook. Para ativar sua conta e começar a gerenciar suas leituras, " +
                "por favor clique no link abaixo:\n\n" +
                link + "\n\n" +
                "Este link é válido por 24 horas.";
        EmailRequestDTO request = new EmailRequestDTO(toEmail, subject, message);
        this.send(request);
    }

    public void sendReadingReminderEmail(String toEmail, String username) {
        String subject = "TimerBook - Hora de voltar a ler";
        String greeting = (username == null || username.isBlank()) ? "Olá!" : "Olá, <strong>" + escapeHtml(username) + "</strong>!";
        String message = "<html><body style=\"font-family:Arial,sans-serif;line-height:1.6;color:#1f2937;\">"
                + "<p>" + greeting + "</p>"
                + "<p>Percebemos que faz algum tempo desde sua última leitura no TimerBook. "
                + "Que tal separar alguns minutos para continuar sua meta diária?</p>"
                + "<p>Se quiser, abra o app e registre sua próxima sessão de leitura.</p>"
                + "<p>Boas leituras,<br/>Equipe TimerBook</p>"
                + "</body></html>";
        EmailRequestDTO request = new EmailRequestDTO(toEmail, subject, message);
        this.send(request);
    }

    public void sendPaymentReceivedEmail(String toEmail, String username, BigDecimal amount, String currency) {
        String subject = "TimerBook - Pagamento recebido";
        String message = buildPaymentEmailHtml(
                username,
                "Recebemos seu pagamento",
                "Seu pagamento foi identificado e está em processamento pela operadora.",
                amount,
                currency,
                "Assim que a confirmação for concluída, enviaremos outra atualização por e-mail."
        );
        EmailRequestDTO request = new EmailRequestDTO(toEmail, subject, message);
        this.send(request);
    }

    public void sendPaymentApprovedEmail(String toEmail, String username, BigDecimal amount, String currency) {
        sendPaymentApprovedEmail(toEmail, username, amount, currency, null, null, null);
    }

    public void sendPaymentApprovedEmail(
            String toEmail,
            String username,
            BigDecimal amount,
            String currency,
            String providerPaymentId,
            String provider,
            LocalDateTime paidAt
    ) {
        String subject = "TimerBook - Comprovante de pagamento";
        String message = buildPaymentReceiptEmailHtml(
                username,
                amount,
                currency,
                providerPaymentId,
                provider,
                paidAt
        );
        EmailRequestDTO request = new EmailRequestDTO(toEmail, subject, message);
        this.send(request);
    }

    public void sendPaymentFailedEmail(String toEmail, String username, BigDecimal amount, String currency, String reason) {
        String subject = "TimerBook - Pagamento não concluído";
        String safeReason = (reason == null || reason.isBlank()) ? "Não foi possível concluir o pagamento." : reason;
        String message = buildPaymentEmailHtml(
                username,
                "Pagamento não concluído",
                safeReason,
                amount,
                currency,
                "Se quiser, revise os dados do pagamento ou tente novamente no portal de assinatura."
        );
        EmailRequestDTO request = new EmailRequestDTO(toEmail, subject, message);
        this.send(request);
    }

    private String escapeHtml(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String buildPaymentEmailHtml(String username, String headline, String message, BigDecimal amount, String currency, String footerMessage) {
        String greeting = (username == null || username.isBlank()) ? "Olá!" : "Olá, <strong>" + escapeHtml(username) + "</strong>!";
        String formattedAmount = formatAmount(amount, currency);
        return "<html><body style=\"font-family:Arial,sans-serif;line-height:1.6;color:#1f2937;\">"
                + "<p>" + greeting + "</p>"
                + "<h2 style=\"margin:0 0 12px 0;color:#111827;\">" + escapeHtml(headline) + "</h2>"
                + "<p>" + escapeHtml(message) + "</p>"
                + "<div style=\"background:#f8fafc;border:1px solid #e5e7eb;border-radius:12px;padding:16px;margin:20px 0;\">"
                + "<p style=\"margin:0 0 4px 0;color:#6b7280;font-size:14px;\">Valor pago</p>"
                + "<p style=\"margin:0;font-size:22px;font-weight:700;color:#111827;\">" + escapeHtml(formattedAmount) + "</p>"
                + "</div>"
                + "<p>" + escapeHtml(footerMessage) + "</p>"
                + "<p>Boas leituras,<br/>Equipe TimerBook</p>"
                + "</body></html>";
    }

    private String buildPaymentReceiptEmailHtml(
            String username,
            BigDecimal amount,
            String currency,
            String providerPaymentId,
            String provider,
            LocalDateTime paidAt
    ) {
        String greeting = (username == null || username.isBlank()) ? "Olá!" : "Olá, <strong>" + escapeHtml(username) + "</strong>!";
        String formattedAmount = formatAmount(amount, currency);
        String paymentCode = providerPaymentId == null || providerPaymentId.isBlank() ? "-" : providerPaymentId;
        String providerName = provider == null || provider.isBlank() ? "Mercado Pago" : provider;
        String paidAtText = formatDateTime(paidAt);

        return "<html><body style=\"font-family:Arial,sans-serif;line-height:1.6;color:#1f2937;\">"
                + "<p>" + greeting + "</p>"
                + "<h2 style=\"margin:0 0 12px 0;color:#111827;\">Comprovante de pagamento</h2>"
                + "<p>Seu pagamento foi aprovado com sucesso. Sua assinatura Premium mensal já está liberada.</p>"
                + "<div style=\"background:#f8fafc;border:1px solid #e5e7eb;border-radius:12px;padding:16px;margin:20px 0;\">"
                + "<p style=\"margin:0 0 4px 0;color:#6b7280;font-size:14px;\">Valor pago</p>"
                + "<p style=\"margin:0;font-size:22px;font-weight:700;color:#111827;\">" + escapeHtml(formattedAmount) + "</p>"
                + "</div>"
                + "<div style=\"border:1px solid #e5e7eb;border-radius:12px;padding:16px;margin:20px 0;\">"
                + "<p style=\"margin:0 0 8px 0;\"><strong>Plano:</strong> Premium mensal</p>"
                + "<p style=\"margin:0 0 8px 0;\"><strong>Status:</strong> Aprovado</p>"
                + "<p style=\"margin:0 0 8px 0;\"><strong>Provedor:</strong> " + escapeHtml(providerName) + "</p>"
                + "<p style=\"margin:0 0 8px 0;\"><strong>Código do pagamento:</strong> " + escapeHtml(paymentCode) + "</p>"
                + "<p style=\"margin:0;\"><strong>Data de aprovação:</strong> " + escapeHtml(paidAtText) + "</p>"
                + "</div>"
                + "<p>Guarde este e-mail como comprovante da sua assinatura.</p>"
                + "<p>Boas leituras,<br/>Equipe TimerBook</p>"
                + "</body></html>";
    }

    private String formatAmount(BigDecimal amount, String currency) {
        if (amount == null) {
            return currency == null || currency.isBlank() ? "-" : currency;
        }

        String normalizedCurrency = currency == null ? "" : currency.trim().toUpperCase(Locale.ROOT);
        if ("BRL".equals(normalizedCurrency)) {
            NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));
            return formatter.format(amount);
        }

        return amount.toPlainString() + (normalizedCurrency.isBlank() ? "" : " " + normalizedCurrency);
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "-";
        }

        return dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}
