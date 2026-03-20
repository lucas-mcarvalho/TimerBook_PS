package com.timerbook.TimerBook.services;

import com.timerbook.TimerBook.dto.ReadingStatsDTO;
import com.timerbook.TimerBook.models.Reading;
import com.timerbook.TimerBook.models.ReadingSession;
import com.timerbook.TimerBook.repository.ReadingRepository;
import com.timerbook.TimerBook.repository.ReadingSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReadingStatsService {
    @Autowired
    private ReadingSessionRepository readingSessionRepository;

    @Autowired
    private ReadingRepository readingRepository;

    public ReadingStatsDTO getStatsForReading(Long readingId, LocalDateTime start, LocalDateTime end, boolean includeOnGoingSessions) {
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

        long sessionsCount = readingSessionRepository.countByReadingBookIdAndStartedAtBetween(readingId, start, end);

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

    public List<Reading> getReadingsInProgress() {
        return readingRepository.findAll()
                .stream()
                .filter(r -> r.getFinishedAt() == null)
                .collect(Collectors.toList());
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
