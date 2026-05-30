package com.timerbook.TimerBook.repository;

import com.timerbook.TimerBook.models.billing.WebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WebhookEventRepository extends JpaRepository<WebhookEvent, Long> {
    boolean existsByProviderEventId(String providerEventId);
    Optional<WebhookEvent> findByProviderEventId(String providerEventId);
}
