package com.timerbook.TimerBook.dto;

public record RegisterRequestDTO (String username, String email, String password, String photopath, Integer dailyReadingGoalMinutes){}
