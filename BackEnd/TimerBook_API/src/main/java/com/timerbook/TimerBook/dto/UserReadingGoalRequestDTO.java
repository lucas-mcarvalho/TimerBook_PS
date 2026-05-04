package com.timerbook.TimerBook.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;

public class UserReadingGoalRequestDTO {

    @Schema(description = "Meta diária de leitura em minutos", example = "20")
    @Positive
    private Integer dailyReadingGoalMinutes;

    public Integer getDailyReadingGoalMinutes() {
        return dailyReadingGoalMinutes;
    }

    public void setDailyReadingGoalMinutes(Integer dailyReadingGoalMinutes) {
        this.dailyReadingGoalMinutes = dailyReadingGoalMinutes;
    }
}