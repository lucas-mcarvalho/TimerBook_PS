package com.timerbook.TimerBook.repository;

import com.timerbook.TimerBook.models.User;
import com.timerbook.TimerBook.models.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserAchievementRepository extends JpaRepository<UserAchievement,Long> {

    boolean existsByUserAndAchievement_KeyCode(User user, String keyCode);

    List<UserAchievement> findByUserId(Long userId);
}
