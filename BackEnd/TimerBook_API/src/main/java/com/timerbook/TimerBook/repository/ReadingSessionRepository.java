package com.timerbook.TimerBook.repository;

import com.timerbook.TimerBook.models.ReadingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReadingSessionRepository extends JpaRepository<ReadingSession, Long> {
    List<ReadingSession> findByBookId(Long bookId);

    List<ReadingSession> findByBookIdAndStartTimeBetween(Long bookId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT SUM(r.durationSeconds) FROM ReadingSession r WHERE r.book.id = :bookId AND r.startTime BETWEEN :start AND :end")
    Long sumDurationByBookIdAndPeriod(@Param("bookId") Long bookId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT SUM(rs.pagesRead) FROM ReadingSession rs WHERE rs.book.id = :bookId AND rs.startTime BETWEEN :start AND :end")
    Integer sumPagesReadByBookIdAndStartTimeBetween(Long bookId, LocalDateTime start, LocalDateTime end);
}
