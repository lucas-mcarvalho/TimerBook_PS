package com.timerbook.TimerBook.services;

import com.timerbook.TimerBook.dto.EmailRequestDTO;
import com.timerbook.TimerBook.models.billing.UserSubscription;
import com.timerbook.TimerBook.repository.UserRepository;
import com.timerbook.TimerBook.repository.UserSubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SubscriptionExpiryService {
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionExpiryService.class);

    @Autowired
    private UserSubscriptionRepository userSubscriptionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    /**
     * Runs daily (midnight UTC by default) to expire subscriptions whose currentPeriodEnd is before now.
     * Cron and timezone can be configured via properties:
     * - billing.subscription.expiry.cron
     * - billing.subscription.timezone
     */
    @Scheduled(cron = "${billing.subscription.expiry.cron:0 0 0 * * *}", zone = "${billing.subscription.timezone:UTC}")
    @Transactional
    public void expireSubscriptions() {
        LocalDateTime now = LocalDateTime.now();
        List<UserSubscription> expired = userSubscriptionRepository.findByStatusAndCurrentPeriodEndBefore("ACTIVE", now);
        if (expired == null || expired.isEmpty()) {
            logger.debug("No expired subscriptions to process at {}", now);
            return;
        }

        for (UserSubscription subscription : expired) {
            try {
                Long userId = subscription.getUser() != null ? subscription.getUser().getId() : null;
                if (userId != null) {
                    userRepository.findById(userId).ifPresent(user -> {
                        user.setSubscriptionPlan("FREE");
                        userRepository.save(user);
                        try {
                            String to = user.getEmail();
                            if (to != null && !to.isBlank()) {
                                String subject = "TimerBook - Sua assinatura expirou";
                                String message = "Olá " + (user.getUsername() == null ? "" : user.getUsername()) + ",\n\n" +
                                        "Sua assinatura expirou em " + (subscription.getCurrentPeriodEnd() != null ? subscription.getCurrentPeriodEnd().toString() : "(data desconhecida)") + ".\n" +
                                        "Seu acesso premium foi suspenso. Se desejar renovar, acesse o portal de assinatura.\n\n" +
                                        "Equipe TimerBook";
                                emailService.send(new EmailRequestDTO(to, subject, message));
                            }
                        } catch (Exception ex) {
                            logger.warn("Falha ao enviar e-mail de expiração para usuário {}", userId, ex);
                        }
                    });
                }

                subscription.setStatus("CANCELED");
                subscription.setCanceledAt(now);
                subscription.setUpdatedAt(LocalDateTime.now());
                userSubscriptionRepository.save(subscription);
                logger.info("Processed expired subscription id={} userId={}", subscription.getId(), userId);
            } catch (Exception ex) {
                logger.error("Erro ao processar expiração da assinatura id={}", subscription.getId(), ex);
            }
        }
    }
}
