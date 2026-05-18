package com.timerbook.TimerBook.integration;

import com.timerbook.TimerBook.models.User;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserAndBookControllerIntegrationTest extends AbstractIntegrationTest {

    @Test
    void userMeAndReadingGoalShouldUseBearerTokenAgainstRealRepository() throws Exception {
        User user = createEnabledUser("reader", "reader@mail.com", "secret123");
        String bearer = bearerFor(user);

        mockMvc.perform(get("/user/me").header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.email").value("reader@mail.com"));

        mockMvc.perform(put("/user/me/reading-goal")
                        .header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "dailyReadingGoalMinutes": 30
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dailyReadingGoalMinutes").value(30));

        assertEquals(30, userRepository.findById(user.getId()).orElseThrow().getDailyReadingGoalMinutes());
    }

    @Test
    void updateReadingGoalShouldReturnBadRequestForInvalidGoal() throws Exception {
        User user = createEnabledUser("reader", "reader@mail.com", "secret123");

        mockMvc.perform(put("/user/me/reading-goal")
                        .header("Authorization", bearerFor(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "dailyReadingGoalMinutes": 20
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCustomReadingGoalShouldAllowPaidUserToUseCustomValue() throws Exception {
        User user = createEnabledUser("reader", "reader@mail.com", "secret123");
        user.setSubscriptionPlan("PAID");
        userRepository.saveAndFlush(user);

        mockMvc.perform(put("/user/me/reading-goal/custom")
                        .header("Authorization", bearerFor(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "dailyReadingGoalMinutes": 25
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dailyReadingGoalMinutes").value(25));

        assertEquals(25, userRepository.findById(user.getId()).orElseThrow().getDailyReadingGoalMinutes());
    }

    @Test
    void updateCustomReadingGoalShouldRejectFreeUser() throws Exception {
        User user = createEnabledUser("reader", "reader@mail.com", "secret123");

        mockMvc.perform(put("/user/me/reading-goal/custom")
                        .header("Authorization", bearerFor(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "dailyReadingGoalMinutes": 25
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.dailyReadingGoalMinutes").doesNotExist());
    }

    @Test
    void protectedBookEndpointShouldRejectMissingToken() throws Exception {
        mockMvc.perform(get("/book"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void bookLifecycleShouldPassThroughSecurityControllerServiceAndRepository() throws Exception {
        User user = createEnabledUser("reader", "reader@mail.com", "secret123");
        String bearer = bearerFor(user);

        String createdBookJson = mockMvc.perform(multipart("/book/create")
                        .file(textPart("name", "Memórias do Subsolo"))
                        .file(textPart("description", "Clássico russo"))
                        .param("userId", user.getId().toString())
                        .header("Authorization", bearer))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Memórias do Subsolo"))
                .andExpect(jsonPath("$.description").value("Clássico russo"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long bookId = objectMapper.readTree(createdBookJson).get("id").asLong();
        assertTrue(bookRepository.findById(bookId).isPresent());

        mockMvc.perform(get("/book/user/{userId}", user.getId())
                        .header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(bookId))
                .andExpect(jsonPath("$[0].name").value("Memórias do Subsolo"));

        mockMvc.perform(multipart("/book/{id}", bookId)
                        .file(textPart("name", "Memórias do Subsolo - Revisado"))
                        .file(textPart("description", "Descrição atualizada"))
                        .header("Authorization", bearer)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Memórias do Subsolo - Revisado"));

        assertEquals("Memórias do Subsolo - Revisado", bookRepository.findById(bookId).orElseThrow().getName());

        mockMvc.perform(delete("/book/{id}", bookId)
                        .header("Authorization", bearer))
                .andExpect(status().isNoContent());

        assertTrue(bookRepository.findById(bookId).isEmpty());
    }

    private MockMultipartFile textPart(String name, String value) {
        return new MockMultipartFile(name, "", "text/plain", value.getBytes(StandardCharsets.UTF_8));
    }
}
