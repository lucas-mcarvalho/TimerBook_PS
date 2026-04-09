package com.timerbook.TimerBook.repository;

import com.timerbook.TimerBook.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Long> {
}
