package com.timerbook.TimerBook.services;

import com.timerbook.TimerBook.dto.AchievementDTO;
import com.timerbook.TimerBook.models.Achievement;
import com.timerbook.TimerBook.models.User;
import com.timerbook.TimerBook.models.UserAchievement;
import com.timerbook.TimerBook.repository.AchievementRepository;
import com.timerbook.TimerBook.repository.ReadingRepository;
import com.timerbook.TimerBook.repository.UserAchievementRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AchievementServiceTest {

    @Mock
    private AchievementRepository achievementRepo;

    @Mock
    private ReadingRepository readingRepository;

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

    private User user() {
        User user = new User();
        user.setId(1L);
        user.setUsername("reader");
        return user;
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
