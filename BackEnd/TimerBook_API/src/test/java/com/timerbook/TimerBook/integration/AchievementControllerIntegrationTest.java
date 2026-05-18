package com.timerbook.TimerBook.integration;

import com.timerbook.TimerBook.models.Achievement;
import com.timerbook.TimerBook.models.User;
import com.timerbook.TimerBook.models.UserAchievement;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AchievementControllerIntegrationTest extends AbstractIntegrationTest {

    @Test
    void getUserMedalsShouldReturnAchievementsPersistedForUser() throws Exception {
        User user = createEnabledUser("reader", "reader@mail.com", "secret123");
        Achievement achievement = new Achievement();
        achievement.setKeyCode("FIRST_LOGIN");
        achievement.setName("Primeiro login");
        achievement.setDescription("Entrou pela primeira vez");
        achievement.setIconUrl("login.svg");
        achievement = achievementRepository.saveAndFlush(achievement);
        userAchievementRepository.saveAndFlush(new UserAchievement(user, achievement));

        mockMvc.perform(get("/achievements/user/{userId}", user.getId())
                        .header("Authorization", bearerFor(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Primeiro login"))
                .andExpect(jsonPath("$[0].icone").value("login.svg"))
                .andExpect(jsonPath("$[0].description").value("Entrou pela primeira vez"));
    }

    @Test
    void getUserMedalsShouldRejectMissingToken() throws Exception {
        mockMvc.perform(get("/achievements/user/1"))
                .andExpect(status().isUnauthorized());
    }
}
