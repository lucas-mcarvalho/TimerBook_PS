package com.timerbook.TimerBook.services;

import com.timerbook.TimerBook.models.User;
import com.timerbook.TimerBook.repository.ReadingSessionRepository;
import com.timerbook.TimerBook.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

@Service
@ConditionalOnProperty(name = "app.reminders.reading.enabled", havingValue = "true", matchIfMissing = true)
public class ReadingReminderService {
    private static final Logger log = LoggerFactory.getLogger(ReadingReminderService.class);

    private final UserRepository userRepository;
    private final ReadingSessionRepository readingSessionRepository;
    private final EmailService emailService;

    @Value("${app.reminders.reading.inactivity-hours:24}")
    private long inactivityHours;

    @Value("${app.reminders.reading.reminder-interval-minutes:1440}")
    private long reminderIntervalMinutes;

    @Value("${app.reminders.reading.timezone:America/Sao_Paulo}")
    private String reminderTimezone;

    public ReadingReminderService(UserRepository userRepository,
                                  ReadingSessionRepository readingSessionRepository,
                                  EmailService emailService) {
        this.userRepository = userRepository;
        this.readingSessionRepository = readingSessionRepository;
        this.emailService = emailService;
    }

    @Scheduled(cron = "${app.reminders.reading.cron:0 0 * * * *}", zone = "${app.reminders.reading.timezone:America/Sao_Paulo}")
    public void sendReadingReminders() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime cutoff = now.minusHours(inactivityHours);
        LocalDateTime reminderCutoff = now.minusMinutes(reminderIntervalMinutes);

        log.info("Running reading reminder scan at {} UTC", now);

        List<User> enabledUsers = userRepository.findByEnabledTrue();
        int sentCount = 0;

        for (User user : enabledUsers) {
            String email = user.getEmail();
            if (email == null || email.isBlank()) {
                continue;
            }

            LocalDateTime lastReadingActivityAt = readingSessionRepository.findLastReadingActivityAtByUserId(user.getId());
            LocalDateTime lastReminderSentAt = user.getLastReadingReminderSentAt();

            boolean inactiveEnough = lastReadingActivityAt == null || lastReadingActivityAt.isBefore(cutoff);
            boolean reminderExpired = lastReminderSentAt == null || lastReminderSentAt.isBefore(reminderCutoff);

            if (!inactiveEnough || !reminderExpired) {
                continue;
            }

            try {
                emailService.sendReadingReminderEmail(email, user.getUsername());
                user.setLastReadingReminderSentAt(now);
                userRepository.save(user);
                sentCount++;
            } catch (RuntimeException e) {
                log.warn("Failed to send reading reminder to user {} ({})", user.getId(), email, e);
            }
        }

        if (sentCount > 0) {
            log.info("Sent {} reading reminder emails", sentCount);
        } else {
            log.info("No reading reminders were sent on this run");
        }
    }
}