package com.timerbook.TimerBook.dto;

public class UserReadingGoalResponseDTO {

    private Integer dailyReadingGoalMinutes;

    public UserReadingGoalResponseDTO() {
    }

    public UserReadingGoalResponseDTO(Integer dailyReadingGoalMinutes) {
        this.dailyReadingGoalMinutes = dailyReadingGoalMinutes;
    }

    public Integer getDailyReadingGoalMinutes() {
        return dailyReadingGoalMinutes;
    }

    public void setDailyReadingGoalMinutes(Integer dailyReadingGoalMinutes) {
        this.dailyReadingGoalMinutes = dailyReadingGoalMinutes;
    }
}