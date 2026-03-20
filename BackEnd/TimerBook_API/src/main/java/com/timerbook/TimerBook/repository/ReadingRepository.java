package com.timerbook.TimerBook.repository;

import com.timerbook.TimerBook.models.Reading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface ReadingRepository extends JpaRepository<Reading, Long> {
    long countByFinishedAtIsNull();
}
