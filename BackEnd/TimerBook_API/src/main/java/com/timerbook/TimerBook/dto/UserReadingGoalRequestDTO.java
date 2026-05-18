package com.timerbook.TimerBook.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;

public class UserReadingGoalRequestDTO {

    @Schema(
            description = "Meta diária de leitura em minutos. Valores permitidos: 5, 10, 15, 30 e 60",
            example = "15",
            allowableValues = {"5", "10", "15", "30", "60"}
    )
    @Positive
    private Integer dailyReadingGoalMinutes;

    public Integer getDailyReadingGoalMinutes() {
        return dailyReadingGoalMinutes;
    }

    public void setDailyReadingGoalMinutes(Integer dailyReadingGoalMinutes) {
        this.dailyReadingGoalMinutes = dailyReadingGoalMinutes;
    }
}