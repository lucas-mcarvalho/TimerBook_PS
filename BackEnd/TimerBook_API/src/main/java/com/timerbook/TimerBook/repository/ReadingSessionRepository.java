package com.timerbook.TimerBook.repository;

import com.timerbook.TimerBook.models.ReadingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface ReadingSessionRepository extends JpaRepository<ReadingSession, Long> {
        long countByReadingIdAndStartedAtBetween(Long readingId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT COALESCE(SUM(rs.endPage - rs.startPage), 0) " +
            "FROM ReadingSession rs " +
            "WHERE rs.reading.id = :readingId " +
            "AND rs.startedAt BETWEEN :start AND :end " +
            "AND rs.endedAt IS NOT NULL " +
            "AND rs.startPage IS NOT NULL " +
            "AND rs.endPage IS NOT NULL " +
            "AND rs.endPage >= rs.startPage")
    Integer sumPagesReadByReadingAndPeriod(
            @Param("readingId") Long readingId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

        @Query(value = "SELECT COALESCE(CAST(SUM(EXTRACT(EPOCH FROM (rs.ended_at - rs.started_at))) AS BIGINT), 0) " +
            "FROM reading_session rs " +
            "WHERE rs.reading_id = :readingId " +
            "AND rs.started_at BETWEEN :start AND :end",
            nativeQuery = true)
    Long sumDurationSecondsByReadingAndPeriod(
            @Param("readingId") Long readingId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT rs FROM ReadingSession rs WHERE rs.reading.id = :readingId AND rs.startedAt BETWEEN :start AND :end ORDER BY rs.startedAt ASC")
    List<ReadingSession> findByReadingIdAndPeriod(@Param("readingId") Long readingId,
                                                  @Param("start") LocalDateTime start,
                                                  @Param("end") LocalDateTime end);

        @Query(value = "SELECT COALESCE(CAST(SUM(EXTRACT(EPOCH FROM (COALESCE(rs.ended_at, CURRENT_TIMESTAMP) - rs.started_at))) AS BIGINT), 0) " +
            "FROM reading_session rs " +
            "WHERE rs.reading_id = :readingId " +
            "AND rs.started_at BETWEEN :start AND :end",
            nativeQuery = true)
    Long sumDurationSecondsByReadingAndPeriodIncludingOngoing(
            @Param("readingId") Long readingId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    List<ReadingSession> findByReadingIdOrderByStartedAtAsc(Long readingId);

    List<ReadingSession> findByReadingId(Long readingId);
}