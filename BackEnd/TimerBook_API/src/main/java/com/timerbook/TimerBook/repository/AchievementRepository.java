package com.timerbook.TimerBook.repository;

import com.timerbook.TimerBook.models.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AchievementRepository extends JpaRepository<Achievement,Long> {

    Optional<Achievement> findByKeyCode(String keyCode);
}
