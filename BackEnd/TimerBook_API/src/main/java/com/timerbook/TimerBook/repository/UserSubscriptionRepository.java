package com.timerbook.TimerBook.repository;

import com.timerbook.TimerBook.models.billing.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {
    Optional<UserSubscription> findByUserId(Long userId);
    Optional<UserSubscription> findByProviderSubscriptionId(String providerSubscriptionId);
}
