package com.timerbook.TimerBook.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class UserCustomReadingGoalRequestDTO {

    @Schema(
            description = "Meta diária de leitura em minutos definida livremente pelo usuário",
            example = "25"
    )
    @NotNull
    @Positive
    private Integer dailyReadingGoalMinutes;

    public Integer getDailyReadingGoalMinutes() {
        return dailyReadingGoalMinutes;
    }

    public void setDailyReadingGoalMinutes(Integer dailyReadingGoalMinutes) {
        this.dailyReadingGoalMinutes = dailyReadingGoalMinutes;
    }
}