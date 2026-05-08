package com.timerbook.TimerBook.services;

import com.timerbook.TimerBook.models.User;
import com.timerbook.TimerBook.repository.ReadingSessionRepository;
import com.timerbook.TimerBook.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReadingReminderServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReadingSessionRepository readingSessionRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ReadingReminderService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "inactivityHours", 1L);
        ReflectionTestUtils.setField(service, "reminderIntervalMinutes", 60L);
        ReflectionTestUtils.setField(service, "reminderTimezone", "America/Sao_Paulo");
    }

    @Test
    void sendReadingRemindersShouldSendOnlyForEnabledInactiveUsersWithExpiredReminderWindow() {
        LocalDateTime beforeRun = LocalDateTime.now(ZoneOffset.UTC).minusSeconds(1);
        User eligible = user(1L, "reader", "reader@mail.com");
        eligible.setLastReadingReminderSentAt(LocalDateTime.now(ZoneOffset.UTC).minusMinutes(61));
        User blankEmail = user(2L, "blank", " ");
        User active = user(3L, "active", "active@mail.com");

        when(userRepository.findByEnabledTrue()).thenReturn(List.of(eligible, blankEmail, active));
        when(readingSessionRepository.findLastReadingActivityAtByUserId(1L))
                .thenReturn(LocalDateTime.now(ZoneOffset.UTC).minusHours(2));
        when(readingSessionRepository.findLastReadingActivityAtByUserId(3L))
                .thenReturn(LocalDateTime.now(ZoneOffset.UTC));

        service.sendReadingReminders();

        verify(emailService).sendReadingReminderEmail("reader@mail.com", "reader");
        verify(emailService, never()).sendReadingReminderEmail(eq("active@mail.com"), anyString());
        verify(userRepository).save(eligible);
        assertNotNull(eligible.getLastReadingReminderSentAt());
        assertTrue(eligible.getLastReadingReminderSentAt().isAfter(beforeRun));
        verify(readingSessionRepository, never()).findLastReadingActivityAtByUserId(2L);
    }

    @Test
    void sendReadingRemindersShouldSkipUserRecentlyReminded() {
        User user = user(1L, "reader", "reader@mail.com");
        user.setLastReadingReminderSentAt(LocalDateTime.now(ZoneOffset.UTC).minusMinutes(30));

        when(userRepository.findByEnabledTrue()).thenReturn(List.of(user));
        when(readingSessionRepository.findLastReadingActivityAtByUserId(1L)).thenReturn(null);

        service.sendReadingReminders();

        verify(emailService, never()).sendReadingReminderEmail(anyString(), anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void sendReadingRemindersShouldContinueWhenEmailServiceFails() {
        User user = user(1L, "reader", "reader@mail.com");
        user.setLastReadingReminderSentAt(null);

        when(userRepository.findByEnabledTrue()).thenReturn(List.of(user));
        when(readingSessionRepository.findLastReadingActivityAtByUserId(1L)).thenReturn(null);
        doThrow(new RuntimeException("smtp down"))
                .when(emailService).sendReadingReminderEmail("reader@mail.com", "reader");

        assertDoesNotThrow(() -> service.sendReadingReminders());

        verify(emailService).sendReadingReminderEmail("reader@mail.com", "reader");
        verify(userRepository, never()).save(any());
    }

    private User user(Long id, String username, String email) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setEnabled(true);
        return user;
    }
}
