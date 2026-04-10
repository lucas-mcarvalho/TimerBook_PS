package com.timerbook.TimerBook.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.timerbook.TimerBook.models.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByAuthority(String authority);
}
