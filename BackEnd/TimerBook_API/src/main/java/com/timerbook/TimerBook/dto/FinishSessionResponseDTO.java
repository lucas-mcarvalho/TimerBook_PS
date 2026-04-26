package com.timerbook.TimerBook.dto;

import com.timerbook.TimerBook.models.ReadingSession;
import java.util.List;

public record FinishSessionResponseDTO(
        ReadingSession session,
        List<AchievementDTO> novasConquistas
) {}