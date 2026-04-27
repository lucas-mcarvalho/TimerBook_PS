package com.timerbook.TimerBook.dto;

public class UserReadingGoalStreakDTO {

    private Integer dailyGoalMinutes;
    private Long todaySeconds;
    private Boolean todayGoalReached;
    private Integer currentStreakDays;
    private Integer maxStreakDays;

    public Integer getDailyGoalMinutes() {
        return dailyGoalMinutes;
    }

    public void setDailyGoalMinutes(Integer dailyGoalMinutes) {
        this.dailyGoalMinutes = dailyGoalMinutes;
    }

    public Long getTodaySeconds() {
        return todaySeconds;
    }

    public void setTodaySeconds(Long todaySeconds) {
        this.todaySeconds = todaySeconds;
    }

    public Boolean getTodayGoalReached() {
        return todayGoalReached;
    }

    public void setTodayGoalReached(Boolean todayGoalReached) {
        this.todayGoalReached = todayGoalReached;
    }

    public Integer getCurrentStreakDays() {
        return currentStreakDays;
    }

    public void setCurrentStreakDays(Integer currentStreakDays) {
        this.currentStreakDays = currentStreakDays;
    }

    public Integer getMaxStreakDays() {
        return maxStreakDays;
    }

    public void setMaxStreakDays(Integer maxStreakDays) {
        this.maxStreakDays = maxStreakDays;
    }
}