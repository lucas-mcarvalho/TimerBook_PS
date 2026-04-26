package com.timerbook.TimerBook.services;

import com.timerbook.TimerBook.dto.ReadingStatsDTO;
import com.timerbook.TimerBook.dto.UserReadingGoalStreakDTO;
import com.timerbook.TimerBook.models.User;
import com.timerbook.TimerBook.models.Reading;
import com.timerbook.TimerBook.models.ReadingSession;
import com.timerbook.TimerBook.repository.ReadingRepository;
import com.timerbook.TimerBook.repository.ReadingSessionRepository;
import com.timerbook.TimerBook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReadingStatsService {
    @Autowired
    private ReadingSessionRepository readingSessionRepository;


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReadingRepository readingRepository;

    public Reading create() {
        Reading r = new Reading();
        r.setBook(null);
        r.setCurrentPage(0);
        r.setStartedAt(LocalDateTime.now());
        r.setFinishedAt(null);
        return readingRepository.save(r);
    }

    public ReadingSession createSession() {
        ReadingSession s = new ReadingSession();
        s.setReading(null);
        s.setStartPage(0);
        s.setEndPage(0);
        s.setStartedAt(LocalDateTime.now());
        s.setEndedAt(null);
        return readingSessionRepository.save(s);
    }

    public Reading update(Long readingId, Integer currentPage, LocalDateTime startedAt, LocalDateTime finishedAt) {
        Optional<Reading> opt = readingRepository.findById(readingId);
        if (opt.isEmpty()) {
            throw new RuntimeException("Reading not found");
        }
        Reading r = opt.get();
        if (currentPage != null) r.setCurrentPage(currentPage);
        if (startedAt != null) r.setStartedAt(startedAt);
        if (finishedAt != null) r.setFinishedAt(finishedAt);
        return readingRepository.save(r);
    }

    public ReadingSession updateSession(Long sessionId, Long readingId, Integer startPage, Integer endPage) {
        Optional<ReadingSession> opt = readingSessionRepository.findById(sessionId);
        if (opt.isEmpty()) {
            throw new RuntimeException("Session not found");
        }
        ReadingSession s = opt.get();
        if (readingId != null) {
            Optional<Reading> rOpt = readingRepository.findById(readingId);
            if (rOpt.isEmpty()) {
                throw new RuntimeException("Reading not found");
            }
            s.setReading(rOpt.get());
        }
        if (startPage != null) s.setStartPage(startPage);
        if (endPage != null) s.setEndPage(endPage);
        s.setEndedAt(LocalDateTime.now());
        return readingSessionRepository.save(s);
    }

    public void delete(Long readingId) {
        readingRepository.deleteById(readingId);
    }

    public void deleteSession(Long sessionId) {
        readingSessionRepository.deleteById(sessionId);
    }

    public ReadingStatsDTO getStatsForReading(Long userId,Long readingId, LocalDateTime start, LocalDateTime end, boolean includeOnGoingSessions) {
        Optional<Reading> optReading = readingRepository.findById(readingId);
        if (optReading.isEmpty() || !optReading.get().getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Leitura não encontrada ou você não tem permissão para acessá-la.");
        }

        if (start == null) {
            start = LocalDateTime.of(2010, 1, 1, 0, 0);
        }
        if (end == null) {
            end = LocalDateTime.now();
        }

        Integer pagesRead = readingSessionRepository.sumPagesReadByReadingAndPeriod(readingId, start, end);
        Long totalSeconds;

        if (includeOnGoingSessions) {
            totalSeconds = readingSessionRepository.sumDurationSecondsByReadingAndPeriodIncludingOngoing(readingId, start, end);
        } else {
            totalSeconds = readingSessionRepository.sumDurationSecondsByReadingAndPeriod(readingId, start, end);
        }

        long sessionsCount = readingSessionRepository.countByReadingIdAndStartedAtBetween(readingId, start, end);

        double avg = (sessionsCount == 0) ? 0.0 : ((double) totalSeconds / (double) sessionsCount);

        // Streaks
        List<ReadingSession> sessions = readingSessionRepository.findByReadingIdAndPeriod(readingId, start, end);
        Set<LocalDate> daysWithSession = sessions.stream()
                .map(rs -> rs.getStartedAt().toLocalDate())
                .collect(Collectors.toCollection(TreeSet::new));

        int currentStreak = calculateCurrentStreak(daysWithSession, end.toLocalDate());
        int maxStreak = calculateMaxStreak(daysWithSession);

        ReadingStatsDTO dto = new ReadingStatsDTO();
        dto.setReadingId(readingId);
        dto.setPagesRead(pagesRead);
        dto.setTotalSeconds(totalSeconds);
        dto.setSessionsCount(sessionsCount);
        dto.setAverageSecondsPerSession(avg);
        dto.setCurrentStreakDays(currentStreak);
        dto.setMaxStreakDays(maxStreak);
        return dto;

    }

    public List<Reading> getReadingsInProgress(Long userId) {
        return readingRepository.findAll()
                .stream()
                .filter(r -> r.getFinishedAt() == null && r.getUser().getId().equals(userId))
                .collect(Collectors.toList());
    }

    public UserReadingGoalStreakDTO getUserGoalStreak(Long userId, LocalDateTime start, LocalDateTime end) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (start == null) {
            start = LocalDateTime.of(2010, 1, 1, 0, 0);
        }
        if (end == null) {
            end = LocalDateTime.now();
        }

        int goalMinutes = user.getDailyReadingGoalMinutes() == null
                ? User.DEFAULT_DAILY_READING_GOAL_MINUTES
                : user.getDailyReadingGoalMinutes();

        long goalSeconds = goalMinutes * 60L;
        List<Object[]> rows = readingSessionRepository.sumDailyDurationSecondsByUserAndPeriod(userId, start, end);

        Map<LocalDate, Long> dailySecondsByDate = new HashMap<>();
        for (Object[] row : rows) {
            Date sqlDate = (Date) row[0];
            Number totalSeconds = (Number) row[1];
            dailySecondsByDate.put(sqlDate.toLocalDate(), totalSeconds.longValue());
        }

        Set<LocalDate> qualifiedDays = dailySecondsByDate.entrySet()
                .stream()
                .filter(e -> e.getValue() >= goalSeconds)
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(TreeSet::new));

        LocalDate endDate = end.toLocalDate();
        int currentStreak = calculateCurrentStreak(qualifiedDays, endDate);
        int maxStreak = calculateMaxStreak(qualifiedDays);

        long todaySeconds = dailySecondsByDate.getOrDefault(endDate, 0L);

        UserReadingGoalStreakDTO dto = new UserReadingGoalStreakDTO();
        dto.setDailyGoalMinutes(goalMinutes);
        dto.setTodaySeconds(todaySeconds);
        dto.setTodayGoalReached(todaySeconds >= goalSeconds);
        dto.setCurrentStreakDays(currentStreak);
        dto.setMaxStreakDays(maxStreak);
        return dto;
    }

    private int calculateCurrentStreak(Set<LocalDate> daysWithSession, LocalDate endDate) {
        if (daysWithSession.isEmpty()) return 0;

        int streak = 0;
        LocalDate dayPointer = endDate;

        while (daysWithSession.contains(dayPointer)) {
            streak++;
            dayPointer = dayPointer.minusDays(1);
        }
        if (streak == 0) {
            Optional<LocalDate> last = daysWithSession.stream().filter(d -> !d.isAfter(endDate)).max(LocalDate::compareTo);
            if (last.isPresent()) {
                LocalDate p = last.get();
                streak = 0;
                while (daysWithSession.contains(p)) {
                    streak++;
                    p = p.minusDays(1);
                }
            }
        }
        return streak;
    }

    private int calculateMaxStreak(Set<LocalDate> daysWithSession) {
        if (daysWithSession.isEmpty()) return 0;
        List<LocalDate> sorted = new ArrayList<>(daysWithSession);
        Collections.sort(sorted);
        int max = 1;
        int cur = 1;
        for (int i = 1; i < sorted.size(); i++) {
            if (sorted.get(i).equals(sorted.get(i-1).plusDays(1))) {
                cur++;
            } else {
                if (cur > max) max = cur;
                cur = 1;
            }
        }
        if (cur > max) max = cur;
        return max;
    }
}
