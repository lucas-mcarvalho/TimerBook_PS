package com.timerbook.TimerBook.dto;

import com.timerbook.TimerBook.models.Reading;
import java.util.List;

public record FinishReadingResponseDTO(
        Reading leitura,
        List<AchievementDTO> novasConquistas
) {}