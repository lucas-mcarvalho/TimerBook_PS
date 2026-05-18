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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class AchievementService {
    private static final LocalDateTime STREAK_SEARCH_START = LocalDateTime.of(2010, 1, 1, 0, 0);
    private static final List<StreakAchievement> READING_STREAK_ACHIEVEMENTS = List.of(
            new StreakAchievement(1, "READING_STREAK_1"),
            new StreakAchievement(10, "READING_STREAK_10"),
            new StreakAchievement(15, "READING_STREAK_15"),
            new StreakAchievement(30, "READING_STREAK_30")
    );
    private static final List<BookCountAchievement> REGISTERED_BOOK_ACHIEVEMENTS = List.of(
            new BookCountAchievement(3, "REGISTERED_BOOKS_3"),
            new BookCountAchievement(10, "REGISTERED_BOOKS_10")
    );

    @Autowired
    private AchievementRepository achievementRepo;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ReadingSessionRepository readingSessionRepository;

    @Autowired
    private UserAchievementRepository userAchievementRepo;

    public List<AchievementDTO> checkFirstLogin(User user) {
        List<AchievementDTO> conquistasDesbloqueadasAgora = new ArrayList<>();
        String keyCode = "FIRST_LOGIN";

        boolean jaPossui = userAchievementRepo.existsByUserAndAchievement_KeyCode(user, keyCode);

        if (!jaPossui) {
            Optional<Achievement> conquistaDb = achievementRepo.findByKeyCode(keyCode);

            if (conquistaDb.isPresent()) {
                Achievement achievement = conquistaDb.get();

                UserAchievement novoGanho = new UserAchievement(user, achievement);
                userAchievementRepo.save(novoGanho);

                conquistasDesbloqueadasAgora.add(
                        new AchievementDTO(achievement.getName(), achievement.getIconUrl(),achievement.getDescription())
                );
            }
        }

        return conquistasDesbloqueadasAgora;
    }

    public List<AchievementDTO> getUserMedals(Long userId) {
        List<UserAchievement> ganhos = userAchievementRepo.findByUserId(userId);

        return ganhos.stream()
                .map(ganho -> new AchievementDTO(
                        ganho.getAchievement().getName(),
                        ganho.getAchievement().getIconUrl(),
                        ganho.getAchievement().getDescription()
                ))
                .toList();
    }

    public List<AchievementDTO> checkFirstSession(User user) {
        List<AchievementDTO> conquistas = new ArrayList<>();
        String keyCode = "FIRST_BOOK";

        boolean jaPossui = userAchievementRepo.existsByUserAndAchievement_KeyCode(user, keyCode);

        if (!jaPossui) {
            Optional<Achievement> conquistaDb = achievementRepo.findByKeyCode(keyCode);

            if (conquistaDb.isPresent()) {
                Achievement achievement = conquistaDb.get();

                UserAchievement novoGanho = new UserAchievement(user, achievement);
                userAchievementRepo.save(novoGanho);

                conquistas.add(
                        new AchievementDTO(achievement.getName(), achievement.getIconUrl(),achievement.getDescription())
                );

                System.out.println("🏆 Conquista de Primeira Sessão desbloqueada para: " + user.getUsername());
            }
        }

        return conquistas;
    }

    public List<AchievementDTO> checkReadingStreak(User user) {
        if (user == null || user.getId() == null) {
            return List.of();
        }

        int maxStreakDays = calculateMaxReadingStreak(user);
        List<AchievementDTO> conquistas = new ArrayList<>();

        for (StreakAchievement streakAchievement : READING_STREAK_ACHIEVEMENTS) {
            if (maxStreakDays >= streakAchievement.days()) {
                unlockAchievement(user, streakAchievement.keyCode()).ifPresent(conquistas::add);
            }
        }

        return conquistas;
    }

    public List<AchievementDTO> checkRegisteredBookMilestones(User user) {
        if (user == null || user.getId() == null) {
            return List.of();
        }

        long registeredBooks = bookRepository.countByUserId(user.getId());
        List<AchievementDTO> conquistas = new ArrayList<>();

        for (BookCountAchievement bookAchievement : REGISTERED_BOOK_ACHIEVEMENTS) {
            if (registeredBooks >= bookAchievement.books()) {
                unlockAchievement(user, bookAchievement.keyCode()).ifPresent(conquistas::add);
            }
        }

        return conquistas;
    }

    public List<AchievementDTO> checkFastBookRead(Reading reading) {
        if (reading == null || reading.getUser() == null || reading.getStartedAt() == null || reading.getFinishedAt() == null) {
            return List.of();
        }

        Duration readingDuration = Duration.between(reading.getStartedAt(), reading.getFinishedAt());
        if (readingDuration.isNegative() || readingDuration.compareTo(Duration.ofDays(1)) >= 0) {
            return List.of();
        }

        return unlockAchievement(reading.getUser(), "FAST_BOOK_READ_UNDER_1_DAY")
                .map(List::of)
                .orElseGet(List::of);
    }

    private int calculateMaxReadingStreak(User user) {
        List<ReadingSession> sessions = readingSessionRepository.findByReadingUserIdAndStartedAtBetweenOrderByStartedAtAsc(
                user.getId(),
                STREAK_SEARCH_START,
                LocalDateTime.now()
        );

        Set<LocalDate> daysWithReading = new HashSet<>();
        for (ReadingSession session : sessions) {
            if (session.getStartedAt() != null) {
                daysWithReading.add(session.getStartedAt().toLocalDate());
            }
        }

        return calculateMaxStreak(daysWithReading);
    }

    private int calculateMaxStreak(Set<LocalDate> days) {
        if (days.isEmpty()) {
            return 0;
        }

        List<LocalDate> sortedDays = new ArrayList<>(days);
        Collections.sort(sortedDays);

        int maxStreak = 1;
        int currentStreak = 1;

        for (int i = 1; i < sortedDays.size(); i++) {
            if (sortedDays.get(i).equals(sortedDays.get(i - 1).plusDays(1))) {
                currentStreak++;
            } else {
                maxStreak = Math.max(maxStreak, currentStreak);
                currentStreak = 1;
            }
        }

        return Math.max(maxStreak, currentStreak);
    }

    private Optional<AchievementDTO> unlockAchievement(User user, String keyCode) {
        boolean jaPossui = userAchievementRepo.existsByUserAndAchievement_KeyCode(user, keyCode);

        if (jaPossui) {
            return Optional.empty();
        }

        return achievementRepo.findByKeyCode(keyCode)
                .map(achievement -> {
                    UserAchievement novoGanho = new UserAchievement(user, achievement);
                    userAchievementRepo.save(novoGanho);

                    return new AchievementDTO(
                            achievement.getName(),
                            achievement.getIconUrl(),
                            achievement.getDescription()
                    );
                });
    }

    private record StreakAchievement(int days, String keyCode) {
    }

    private record BookCountAchievement(int books, String keyCode) {
    }
}
