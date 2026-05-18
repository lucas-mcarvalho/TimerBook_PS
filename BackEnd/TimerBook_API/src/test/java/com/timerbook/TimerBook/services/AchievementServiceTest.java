package com.timerbook.TimerBook.services;

import com.timerbook.TimerBook.dto.AchievementDTO;
import com.timerbook.TimerBook.models.Achievement;
import com.timerbook.TimerBook.models.Reading;
import com.timerbook.TimerBook.models.ReadingSession;
import com.timerbook.TimerBook.models.User;
import com.timerbook.TimerBook.models.UserAchievement;
import com.timerbook.TimerBook.repository.AchievementRepository;
import com.timerbook.TimerBook.repository.BookRepository;
import com.timerbook.TimerBook.repository.ReadingSessionRepository;
import com.timerbook.TimerBook.repository.UserAchievementRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AchievementServiceTest {

    @Mock
    private AchievementRepository achievementRepo;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private ReadingSessionRepository readingSessionRepository;

    @Mock
    private UserAchievementRepository userAchievementRepo;

    @InjectMocks
    private AchievementService service;

    @Test
    void checkFirstLoginShouldUnlockAchievementWhenUserDoesNotHaveIt() {
        User user = user();
        Achievement achievement = achievement("FIRST_LOGIN", "Primeiro login");
        ArgumentCaptor<UserAchievement> captor = ArgumentCaptor.forClass(UserAchievement.class);

        when(userAchievementRepo.existsByUserAndAchievement_KeyCode(user, "FIRST_LOGIN")).thenReturn(false);
        when(achievementRepo.findByKeyCode("FIRST_LOGIN")).thenReturn(Optional.of(achievement));

        List<AchievementDTO> result = service.checkFirstLogin(user);

        assertEquals(1, result.size());
        assertEquals("Primeiro login", result.get(0).getNome());
        assertEquals("login.svg", result.get(0).getIcone());
        assertEquals("Descrição", result.get(0).getDescription());
        verify(userAchievementRepo).save(captor.capture());
        assertSame(user, captor.getValue().getUser());
        assertSame(achievement, captor.getValue().getAchievement());
    }

    @Test
    void checkFirstLoginShouldReturnEmptyWhenAlreadyUnlocked() {
        User user = user();
        when(userAchievementRepo.existsByUserAndAchievement_KeyCode(user, "FIRST_LOGIN")).thenReturn(true);

        List<AchievementDTO> result = service.checkFirstLogin(user);

        assertTrue(result.isEmpty());
        verify(achievementRepo, never()).findByKeyCode(anyString());
        verify(userAchievementRepo, never()).save(any());
    }

    @Test
    void checkFirstLoginShouldReturnEmptyWhenAchievementIsNotConfigured() {
        User user = user();
        when(userAchievementRepo.existsByUserAndAchievement_KeyCode(user, "FIRST_LOGIN")).thenReturn(false);
        when(achievementRepo.findByKeyCode("FIRST_LOGIN")).thenReturn(Optional.empty());

        List<AchievementDTO> result = service.checkFirstLogin(user);

        assertTrue(result.isEmpty());
        verify(userAchievementRepo, never()).save(any());
    }

    @Test
    void getUserMedalsShouldMapUnlockedAchievementsToDtos() {
        Achievement first = achievement("FIRST_LOGIN", "Primeiro login");
        Achievement book = achievement("FIRST_BOOK", "Primeiro livro");
        User user = user();

        when(userAchievementRepo.findByUserId(1L)).thenReturn(List.of(
                new UserAchievement(user, first),
                new UserAchievement(user, book)
        ));

        List<AchievementDTO> result = service.getUserMedals(1L);

        assertEquals(2, result.size());
        assertEquals("Primeiro login", result.get(0).getNome());
        assertEquals("Primeiro livro", result.get(1).getNome());
    }

    @Test
    void checkFirstSessionShouldUnlockFirstBookAchievement() {
        User user = user();
        Achievement achievement = achievement("FIRST_BOOK", "Primeira sessão");

        when(userAchievementRepo.existsByUserAndAchievement_KeyCode(user, "FIRST_BOOK")).thenReturn(false);
        when(achievementRepo.findByKeyCode("FIRST_BOOK")).thenReturn(Optional.of(achievement));

        List<AchievementDTO> result = service.checkFirstSession(user);

        assertEquals(1, result.size());
        assertEquals("Primeira sessão", result.get(0).getNome());
        verify(userAchievementRepo).save(any(UserAchievement.class));
    }

    @Test
    void checkFirstSessionShouldReturnEmptyWhenAlreadyUnlocked() {
        User user = user();
        when(userAchievementRepo.existsByUserAndAchievement_KeyCode(user, "FIRST_BOOK")).thenReturn(true);

        List<AchievementDTO> result = service.checkFirstSession(user);

        assertTrue(result.isEmpty());
        verify(userAchievementRepo, never()).save(any());
    }

    @Test
    void checkReadingStreakShouldUnlockReachedMilestones() {
        User user = user();
        Achievement oneDay = achievement("READING_STREAK_1", "Sequência de 1 dia");
        Achievement tenDays = achievement("READING_STREAK_10", "Sequência de 10 dias");

        when(readingSessionRepository.findByReadingUserIdAndStartedAtBetweenOrderByStartedAtAsc(eq(1L), any(), any()))
                .thenReturn(readingSessionsForConsecutiveDays(10));
        when(userAchievementRepo.existsByUserAndAchievement_KeyCode(user, "READING_STREAK_1")).thenReturn(false);
        when(userAchievementRepo.existsByUserAndAchievement_KeyCode(user, "READING_STREAK_10")).thenReturn(false);
        when(achievementRepo.findByKeyCode("READING_STREAK_1")).thenReturn(Optional.of(oneDay));
        when(achievementRepo.findByKeyCode("READING_STREAK_10")).thenReturn(Optional.of(tenDays));

        List<AchievementDTO> result = service.checkReadingStreak(user);

        assertEquals(2, result.size());
        assertEquals("Sequência de 1 dia", result.get(0).getNome());
        assertEquals("Sequência de 10 dias", result.get(1).getNome());
        verify(userAchievementRepo, times(2)).save(any(UserAchievement.class));
        verify(achievementRepo, never()).findByKeyCode("READING_STREAK_15");
        verify(achievementRepo, never()).findByKeyCode("READING_STREAK_30");
    }

    @Test
    void checkReadingStreakShouldSkipAlreadyUnlockedMilestones() {
        User user = user();
        Achievement fifteenDays = achievement("READING_STREAK_15", "Sequência de 15 dias");

        when(readingSessionRepository.findByReadingUserIdAndStartedAtBetweenOrderByStartedAtAsc(eq(1L), any(), any()))
                .thenReturn(readingSessionsForConsecutiveDays(15));
        when(userAchievementRepo.existsByUserAndAchievement_KeyCode(user, "READING_STREAK_1")).thenReturn(true);
        when(userAchievementRepo.existsByUserAndAchievement_KeyCode(user, "READING_STREAK_10")).thenReturn(true);
        when(userAchievementRepo.existsByUserAndAchievement_KeyCode(user, "READING_STREAK_15")).thenReturn(false);
        when(achievementRepo.findByKeyCode("READING_STREAK_15")).thenReturn(Optional.of(fifteenDays));

        List<AchievementDTO> result = service.checkReadingStreak(user);

        assertEquals(1, result.size());
        assertEquals("Sequência de 15 dias", result.get(0).getNome());
        verify(achievementRepo, never()).findByKeyCode("READING_STREAK_1");
        verify(achievementRepo, never()).findByKeyCode("READING_STREAK_10");
        verify(userAchievementRepo, times(1)).save(any(UserAchievement.class));
    }

    @Test
    void checkRegisteredBookMilestonesShouldUnlockReachedMilestones() {
        User user = user();
        Achievement threeBooks = achievement("REGISTERED_BOOKS_3", "Biblioteca inicial");
        Achievement tenBooks = achievement("REGISTERED_BOOKS_10", "Biblioteca em crescimento");

        when(bookRepository.countByUserId(1L)).thenReturn(10L);
        when(userAchievementRepo.existsByUserAndAchievement_KeyCode(user, "REGISTERED_BOOKS_3")).thenReturn(false);
        when(userAchievementRepo.existsByUserAndAchievement_KeyCode(user, "REGISTERED_BOOKS_10")).thenReturn(false);
        when(achievementRepo.findByKeyCode("REGISTERED_BOOKS_3")).thenReturn(Optional.of(threeBooks));
        when(achievementRepo.findByKeyCode("REGISTERED_BOOKS_10")).thenReturn(Optional.of(tenBooks));

        List<AchievementDTO> result = service.checkRegisteredBookMilestones(user);

        assertEquals(2, result.size());
        assertEquals("Biblioteca inicial", result.get(0).getNome());
        assertEquals("Biblioteca em crescimento", result.get(1).getNome());
        verify(userAchievementRepo, times(2)).save(any(UserAchievement.class));
    }

    @Test
    void checkRegisteredBookMilestonesShouldSkipAlreadyUnlockedMilestones() {
        User user = user();
        Achievement tenBooks = achievement("REGISTERED_BOOKS_10", "Biblioteca em crescimento");

        when(bookRepository.countByUserId(1L)).thenReturn(10L);
        when(userAchievementRepo.existsByUserAndAchievement_KeyCode(user, "REGISTERED_BOOKS_3")).thenReturn(true);
        when(userAchievementRepo.existsByUserAndAchievement_KeyCode(user, "REGISTERED_BOOKS_10")).thenReturn(false);
        when(achievementRepo.findByKeyCode("REGISTERED_BOOKS_10")).thenReturn(Optional.of(tenBooks));

        List<AchievementDTO> result = service.checkRegisteredBookMilestones(user);

        assertEquals(1, result.size());
        assertEquals("Biblioteca em crescimento", result.get(0).getNome());
        verify(achievementRepo, never()).findByKeyCode("REGISTERED_BOOKS_3");
        verify(userAchievementRepo, times(1)).save(any(UserAchievement.class));
    }

    @Test
    void checkFastBookReadShouldUnlockWhenReadingFinishedInLessThanOneDay() {
        User user = user();
        Reading reading = reading(user, LocalDateTime.of(2024, 5, 8, 10, 0), LocalDateTime.of(2024, 5, 9, 9, 59));
        Achievement fastRead = achievement("FAST_BOOK_READ_UNDER_1_DAY", "Leitura relâmpago");

        when(userAchievementRepo.existsByUserAndAchievement_KeyCode(user, "FAST_BOOK_READ_UNDER_1_DAY")).thenReturn(false);
        when(achievementRepo.findByKeyCode("FAST_BOOK_READ_UNDER_1_DAY")).thenReturn(Optional.of(fastRead));

        List<AchievementDTO> result = service.checkFastBookRead(reading);

        assertEquals(1, result.size());
        assertEquals("Leitura relâmpago", result.get(0).getNome());
        verify(userAchievementRepo).save(any(UserAchievement.class));
    }

    @Test
    void checkFastBookReadShouldReturnEmptyWhenReadingTakesOneDayOrMore() {
        Reading reading = reading(user(), LocalDateTime.of(2024, 5, 8, 10, 0), LocalDateTime.of(2024, 5, 9, 10, 0));

        List<AchievementDTO> result = service.checkFastBookRead(reading);

        assertTrue(result.isEmpty());
        verify(userAchievementRepo, never()).existsByUserAndAchievement_KeyCode(any(), anyString());
        verify(achievementRepo, never()).findByKeyCode(anyString());
        verify(userAchievementRepo, never()).save(any());
    }

    private User user() {
        User user = new User();
        user.setId(1L);
        user.setUsername("reader");
        return user;
    }

    private List<ReadingSession> readingSessionsForConsecutiveDays(int days) {
        return IntStream.rangeClosed(1, days)
                .mapToObj(day -> readingSession(LocalDate.of(2024, 5, day).atTime(10, 0)))
                .toList();
    }

    private ReadingSession readingSession(LocalDateTime startedAt) {
        ReadingSession session = new ReadingSession();
        session.setStartedAt(startedAt);
        return session;
    }

    private Reading reading(User user, LocalDateTime startedAt, LocalDateTime finishedAt) {
        Reading reading = new Reading();
        reading.setUser(user);
        reading.setStartedAt(startedAt);
        reading.setFinishedAt(finishedAt);
        return reading;
    }

    private Achievement achievement(String keyCode, String name) {
        Achievement achievement = new Achievement();
        achievement.setId(1L);
        achievement.setKeyCode(keyCode);
        achievement.setName(name);
        achievement.setIconUrl("login.svg");
        achievement.setDescription("Descrição");
        return achievement;
    }
}
