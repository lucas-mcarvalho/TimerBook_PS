package com.timerbook.TimerBook.services;

import com.timerbook.TimerBook.dto.ReadingStatsDTO;
import com.timerbook.TimerBook.dto.UserReadingGoalStreakDTO;
import com.timerbook.TimerBook.models.Book;
import com.timerbook.TimerBook.models.Reading;
import com.timerbook.TimerBook.models.ReadingSession;
import com.timerbook.TimerBook.models.User;
import com.timerbook.TimerBook.repository.ReadingRepository;
import com.timerbook.TimerBook.repository.ReadingSessionRepository;
import com.timerbook.TimerBook.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReadingStatsServiceTest {

    @Mock
    private ReadingSessionRepository readingSessionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReadingRepository readingRepository;

    @InjectMocks
    private ReadingStatsService service;

    @Test
    void createShouldInitializeReadingWithDefaultValues() {
        when(readingRepository.save(any(Reading.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Reading result = service.create();

        assertNull(result.getBook());
        assertEquals(0, result.getCurrentPage());
        assertNotNull(result.getStartedAt());
        assertNull(result.getFinishedAt());
        verify(readingRepository).save(result);
    }

    @Test
    void createSessionShouldInitializeSessionWithDefaultValues() {
        when(readingSessionRepository.save(any(ReadingSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReadingSession result = service.createSession();

        assertNull(result.getReading());
        assertEquals(0, result.getStartPage());
        assertEquals(0, result.getEndPage());
        assertNotNull(result.getStartedAt());
        assertNull(result.getEndedAt());
        verify(readingSessionRepository).save(result);
    }

    @Test
    void updateShouldPatchOnlyProvidedReadingFields() {
        Reading reading = reading(1L, user(1L));
        LocalDateTime startedAt = LocalDateTime.of(2024, 5, 1, 8, 0);
        LocalDateTime finishedAt = LocalDateTime.of(2024, 5, 2, 8, 0);

        when(readingRepository.findById(1L)).thenReturn(Optional.of(reading));
        when(readingRepository.save(reading)).thenReturn(reading);

        Reading result = service.update(1L, 25, startedAt, finishedAt);

        assertEquals(25, result.getCurrentPage());
        assertEquals(startedAt, result.getStartedAt());
        assertEquals(finishedAt, result.getFinishedAt());
        verify(readingRepository).save(reading);
    }

    @Test
    void updateShouldThrowWhenReadingDoesNotExist() {
        when(readingRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.update(1L, 25, null, null));

        assertEquals("Reading not found", exception.getMessage());
    }

    @Test
    void updateSessionShouldPatchFieldsAndAttachReadingWhenProvided() {
        ReadingSession session = session(LocalDate.of(2024, 5, 8));
        Reading reading = reading(99L, user(1L));

        when(readingSessionRepository.findById(10L)).thenReturn(Optional.of(session));
        when(readingRepository.findById(99L)).thenReturn(Optional.of(reading));
        when(readingSessionRepository.save(session)).thenReturn(session);

        ReadingSession result = service.updateSession(10L, 99L, 5, 15);

        assertEquals(reading, result.getReading());
        assertEquals(5, result.getStartPage());
        assertEquals(15, result.getEndPage());
        assertNotNull(result.getEndedAt());
        verify(readingSessionRepository).save(session);
    }

    @Test
    void updateSessionShouldThrowWhenSessionDoesNotExist() {
        when(readingSessionRepository.findById(10L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.updateSession(10L, null, 1, 2));

        assertEquals("Session not found", exception.getMessage());
    }

    @Test
    void updateSessionShouldThrowWhenProvidedReadingDoesNotExist() {
        when(readingSessionRepository.findById(10L)).thenReturn(Optional.of(session(LocalDate.of(2024, 5, 8))));
        when(readingRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.updateSession(10L, 99L, 1, 2));

        assertEquals("Reading not found", exception.getMessage());
    }

    @Test
    void deleteShouldDelegateToReadingRepository() {
        service.delete(1L);

        verify(readingRepository).deleteById(1L);
    }

    @Test
    void deleteSessionShouldDelegateToReadingSessionRepository() {
        service.deleteSession(10L);

        verify(readingSessionRepository).deleteById(10L);
    }

    @Test
    void getStatsForReadingShouldAggregateRepositoryValuesAndCalculateStreaks() {
        User user = user(1L);
        Reading reading = reading(7L, user);
        LocalDateTime start = LocalDateTime.of(2024, 5, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 5, 8, 23, 59);

        when(readingRepository.findById(7L)).thenReturn(Optional.of(reading));
        when(readingSessionRepository.sumPagesReadByReadingAndPeriod(7L, start, end)).thenReturn(42);
        when(readingSessionRepository.sumDurationSecondsByReadingAndPeriodIncludingOngoing(7L, start, end)).thenReturn(3600L);
        when(readingSessionRepository.countByReadingIdAndStartedAtBetween(7L, start, end)).thenReturn(3L);
        when(readingSessionRepository.findByReadingIdAndPeriod(7L, start, end)).thenReturn(List.of(
                session(LocalDate.of(2024, 5, 6)),
                session(LocalDate.of(2024, 5, 7)),
                session(LocalDate.of(2024, 5, 8))
        ));

        ReadingStatsDTO result = service.getStatsForReading(1L, 7L, start, end, true);

        assertEquals(7L, result.getReadingId());
        assertEquals(42, result.getPagesRead());
        assertEquals(3600L, result.getTotalSeconds());
        assertEquals(3L, result.getSessionsCount());
        assertEquals(1200.0, result.getAverageSecondsPerSession());
        assertEquals(3, result.getCurrentStreakDays());
        assertEquals(3, result.getMaxStreakDays());
    }

    @Test
    void getStatsForReadingShouldUseFinishedOnlyDurationWhenOngoingSessionsAreExcluded() {
        User user = user(1L);
        Reading reading = reading(7L, user);
        LocalDateTime start = LocalDateTime.of(2024, 5, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 5, 8, 23, 59);

        when(readingRepository.findById(7L)).thenReturn(Optional.of(reading));
        when(readingSessionRepository.sumPagesReadByReadingAndPeriod(7L, start, end)).thenReturn(0);
        when(readingSessionRepository.sumDurationSecondsByReadingAndPeriod(7L, start, end)).thenReturn(0L);
        when(readingSessionRepository.countByReadingIdAndStartedAtBetween(7L, start, end)).thenReturn(0L);
        when(readingSessionRepository.findByReadingIdAndPeriod(7L, start, end)).thenReturn(List.of());

        ReadingStatsDTO result = service.getStatsForReading(1L, 7L, start, end, false);

        assertEquals(0L, result.getTotalSeconds());
        assertEquals(0.0, result.getAverageSecondsPerSession());
        verify(readingSessionRepository, never()).sumDurationSecondsByReadingAndPeriodIncludingOngoing(any(), any(), any());
    }

    @Test
    void getStatsForReadingShouldRejectMissingOrUnauthorizedReading() {
        Reading reading = reading(7L, user(2L));
        when(readingRepository.findById(7L)).thenReturn(Optional.of(reading));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.getStatsForReading(1L, 7L, null, null, false)
        );

        assertEquals("Leitura não encontrada ou você não tem permissão para acessá-la.", exception.getMessage());
    }

    @Test
    void getGeneralStatsForUserShouldCalculatePagesDurationsAverageAndStreaksFromSessions() {
        User user = user(1L);
        Reading reading = reading(7L, user);
        LocalDateTime start = LocalDateTime.of(2024, 5, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 5, 8, 23, 59);
        ReadingSession completed = session(
                reading,
                LocalDateTime.of(2024, 5, 7, 10, 0),
                LocalDateTime.of(2024, 5, 7, 11, 0),
                10,
                20
        );
        ReadingSession ongoing = session(
                reading,
                LocalDateTime.of(2024, 5, 8, 10, 0),
                null,
                20,
                30
        );

        when(readingSessionRepository.findByReadingUserIdAndStartedAtBetweenOrderByStartedAtAsc(1L, start, end))
                .thenReturn(List.of(completed, ongoing));

        ReadingStatsDTO result = service.getGeneralStatsForUser(1L, start, end, false);

        assertNull(result.getReadingId());
        assertEquals(10, result.getPagesRead());
        assertEquals(3600L, result.getTotalSeconds());
        assertEquals(2L, result.getSessionsCount());
        assertEquals(1800.0, result.getAverageSecondsPerSession());
        assertEquals(2, result.getCurrentStreakDays());
        assertEquals(2, result.getMaxStreakDays());
    }

    @Test
    void getGeneralStatsForUserShouldIgnoreNegativeDurations() {
        User user = user(1L);
        Reading reading = reading(7L, user);
        LocalDateTime start = LocalDateTime.of(2024, 5, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 5, 8, 23, 59);
        ReadingSession inconsistent = session(
                reading,
                LocalDateTime.of(2024, 5, 8, 11, 0),
                LocalDateTime.of(2024, 5, 8, 10, 0),
                1,
                2
        );

        when(readingSessionRepository.findByReadingUserIdAndStartedAtBetweenOrderByStartedAtAsc(1L, start, end))
                .thenReturn(List.of(inconsistent));

        ReadingStatsDTO result = service.getGeneralStatsForUser(1L, start, end, false);

        assertEquals(0L, result.getTotalSeconds());
    }

    @Test
    void getReadingsInProgressShouldReturnOnlyUnfinishedReadingsForUser() {
        User userOne = user(1L);
        User userTwo = user(2L);
        Reading unfinishedForUser = reading(1L, userOne);
        Reading finishedForUser = reading(2L, userOne);
        finishedForUser.setFinishedAt(LocalDateTime.of(2024, 5, 8, 10, 0));
        Reading unfinishedForOtherUser = reading(3L, userTwo);

        when(readingRepository.findAll()).thenReturn(List.of(unfinishedForUser, finishedForUser, unfinishedForOtherUser));

        List<Reading> result = service.getReadingsInProgress(1L);

        assertEquals(List.of(unfinishedForUser), result);
    }

    @Test
    void getUserGoalStreakShouldCalculateGoalProgressAndStreaks() {
        User user = user(1L);
        user.setDailyReadingGoalMinutes(20);
        LocalDateTime start = LocalDateTime.of(2024, 5, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 5, 8, 23, 59);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(readingSessionRepository.sumDailyDurationSecondsByUserAndPeriod(1L, start, end)).thenReturn(List.of(
                new Object[]{Date.valueOf(LocalDate.of(2024, 5, 6)), 600L},
                new Object[]{Date.valueOf(LocalDate.of(2024, 5, 7)), 1200L},
                new Object[]{Date.valueOf(LocalDate.of(2024, 5, 8)), 1800L}
        ));

        UserReadingGoalStreakDTO result = service.getUserGoalStreak(1L, start, end);

        assertEquals(20, result.getDailyGoalMinutes());
        assertEquals(1800L, result.getTodaySeconds());
        assertTrue(result.getTodayGoalReached());
        assertEquals(2, result.getCurrentStreakDays());
        assertEquals(2, result.getMaxStreakDays());
    }

    @Test
    void getUserGoalStreakShouldUseDefaultGoalWhenUserGoalIsNull() {
        User user = user(1L);
        user.setDailyReadingGoalMinutes(null);
        LocalDateTime start = LocalDateTime.of(2024, 5, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 5, 8, 23, 59);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(readingSessionRepository.sumDailyDurationSecondsByUserAndPeriod(1L, start, end)).thenReturn(List.of());

        UserReadingGoalStreakDTO result = service.getUserGoalStreak(1L, start, end);

        assertEquals(User.DEFAULT_DAILY_READING_GOAL_MINUTES, result.getDailyGoalMinutes());
        assertEquals(0L, result.getTodaySeconds());
        assertFalse(result.getTodayGoalReached());
        assertEquals(0, result.getCurrentStreakDays());
        assertEquals(0, result.getMaxStreakDays());
    }

    @Test
    void getUserGoalStreakShouldThrowWhenUserDoesNotExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.getUserGoalStreak(1L, null, null));

        assertEquals("Usuário não encontrado", exception.getMessage());
    }

    private User user(Long id) {
        User user = new User();
        user.setId(id);
        user.setUsername("reader-" + id);
        user.setDailyReadingGoalMinutes(User.DEFAULT_DAILY_READING_GOAL_MINUTES);
        return user;
    }

    private Reading reading(Long id, User user) {
        Book book = new Book();
        book.setId(1L);
        book.setName("Livro");

        Reading reading = new Reading();
        reading.setId(id);
        reading.setUser(user);
        reading.setBook(book);
        reading.setCurrentPage(0);
        reading.setStartedAt(LocalDateTime.of(2024, 5, 1, 10, 0));
        return reading;
    }

    private ReadingSession session(LocalDate date) {
        return session(
                reading(7L, user(1L)),
                date.atTime(10, 0),
                date.atTime(11, 0),
                1,
                2
        );
    }

    private ReadingSession session(Reading reading, LocalDateTime startedAt, LocalDateTime endedAt, Integer startPage, Integer endPage) {
        ReadingSession session = new ReadingSession();
        session.setId(10L);
        session.setReading(reading);
        session.setStartedAt(startedAt);
        session.setEndedAt(endedAt);
        session.setStartPage(startPage);
        session.setEndPage(endPage);
        return session;
    }
}
