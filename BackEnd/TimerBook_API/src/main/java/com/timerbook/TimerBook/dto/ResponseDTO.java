package com.timerbook.TimerBook.dto;

import java.util.List;

public record ResponseDTO (String username,
                           String token, String refreshToken, List<AchievementDTO> novasConquistas) {}
