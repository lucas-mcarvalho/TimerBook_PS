package com.timerbook.TimerBook.repository;

import com.timerbook.TimerBook.models.Reading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface ReadingRepository extends JpaRepository<Reading, Long> {
    long countByFinishedAtIsNull();
    List<Reading> findByBookId(Long bookId);
}
