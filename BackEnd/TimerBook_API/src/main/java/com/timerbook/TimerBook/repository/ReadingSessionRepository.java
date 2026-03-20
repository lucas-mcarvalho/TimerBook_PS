package com.timerbook.TimerBook.repository;

import com.timerbook.TimerBook.models.ReadingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReadingSessionRepository extends JpaRepository<ReadingSession, Long> {

    long countByReadingBookIdAndStartedAtBetween(Long bookId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT COALESCE(SUM(rs.endPage - rs.startPage), 0) " +
            "FROM ReadingSession rs " +
            "WHERE rs.reading.id = :readingId " +
            "AND rs.startedAt BETWEEN :start AND :end")
    Integer sumPagesReadByReadingAndPeriod(
            @Param("readingId") Long readingId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query(value = "SELECT COALESCE(SUM(TIMESTAMPDIFF(SECOND, rs.started_at, rs.ended_at)), 0) " +
            "FROM tb_reading_session rs " +
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

    @Query(value = "SELECT COALESCE(SUM(TIMESTAMPDIFF(SECOND, rs.started_at, COALESCE(rs.ended_at, NOW()))), 0) " +
            "FROM tb_reading_session rs " +
            "WHERE rs.reading_id = :readingId " +
            "AND rs.started_at BETWEEN :start AND :end",
            nativeQuery = true)
    Long sumDurationSecondsByReadingAndPeriodIncludingOngoing(
            @Param("readingId") Long readingId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    List<ReadingSession> findByReadingIdOrderByStartedAtAsc(Long readingId);
}
