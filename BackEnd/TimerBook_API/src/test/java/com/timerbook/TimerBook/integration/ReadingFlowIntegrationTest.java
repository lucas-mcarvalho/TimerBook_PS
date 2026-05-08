package com.timerbook.TimerBook.integration;

import com.timerbook.TimerBook.models.Book;
import com.timerbook.TimerBook.models.Reading;
import com.timerbook.TimerBook.models.ReadingSession;
import com.timerbook.TimerBook.models.User;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReadingFlowIntegrationTest extends AbstractIntegrationTest {

    @Test
    void readingAndSessionFlowShouldUseControllersServicesRepositoriesAndJwtSecurity() throws Exception {
        User user = createEnabledUser("reader", "reader@mail.com", "secret123");
        Book book = createBook(user, "Clean Code");
        String bearer = bearerFor(user);

        String readingJson = mockMvc.perform(post("/readings/{userId}/start", user.getId())
                        .header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bookId": %d,
                                  "startPage": 5
                                }
                                """.formatted(book.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.currentPage").value(5))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long readingId = objectMapper.readTree(readingJson).get("id").asLong();
        Reading savedReading = readingRepository.findById(readingId).orElseThrow();
        assertEquals(user.getId(), savedReading.getUser().getId());
        assertEquals(book.getId(), savedReading.getBook().getId());

        ReadingSession initialSession = readingSessionRepository.findByReadingId(readingId).get(0);
        assertEquals(5, initialSession.getStartPage());
        assertEquals(5, initialSession.getEndPage());

        mockMvc.perform(put("/reading-sessions/{sessionId}/finish", initialSession.getId())
                        .header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "endPage": 15
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.session.id").value(initialSession.getId()))
                .andExpect(jsonPath("$.session.endPage").value(15));

        assertNotNull(readingSessionRepository.findById(initialSession.getId()).orElseThrow().getEndedAt());

        mockMvc.perform(put("/readings/{userId}/{readingId}/finish", user.getId(), readingId)
                        .header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "finalPage": 80,
                                  "notes": "finalizado"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPage").value(80));

        Reading finishedReading = readingRepository.findById(readingId).orElseThrow();
        assertEquals(80, finishedReading.getCurrentPage());
        assertNotNull(finishedReading.getFinishedAt());
    }

    @Test
    void statsGeneralAndBooksInProgressShouldUseLoggedUserFromSecurityContext() throws Exception {
        User user = createEnabledUser("reader", "reader@mail.com", "secret123");
        User otherUser = createEnabledUser("other", "other@mail.com", "secret123");
        Book book = createBook(user, "Domain-Driven Design");
        Book otherBook = createBook(otherUser, "Outro livro");
        Reading reading = createReading(user, book, 25);
        Reading finishedReading = createReading(user, book, 50);
        finishedReading.setFinishedAt(LocalDateTime.of(2026, 5, 9, 10, 0));
        readingRepository.saveAndFlush(finishedReading);
        createReading(otherUser, otherBook, 10);

        createSession(
                reading,
                LocalDateTime.of(2026, 5, 7, 10, 0),
                LocalDateTime.of(2026, 5, 7, 11, 0),
                0,
                10
        );
        createSession(
                reading,
                LocalDateTime.of(2026, 5, 8, 10, 0),
                LocalDateTime.of(2026, 5, 8, 10, 30),
                10,
                20
        );

        String bearer = bearerFor(user);

        mockMvc.perform(get("/stats/user/general")
                        .header("Authorization", bearer)
                        .param("start", "2026-05-01T00:00:00")
                        .param("end", "2026-05-08T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pagesRead").value(20))
                .andExpect(jsonPath("$.totalSeconds").value(5400))
                .andExpect(jsonPath("$.sessionsCount").value(2))
                .andExpect(jsonPath("$.currentStreakDays").value(2))
                .andExpect(jsonPath("$.maxStreakDays").value(2));

        mockMvc.perform(get("/stats/books-in-progress")
                        .header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(reading.getId()))
                .andExpect(jsonPath("$[0].user.id").value(user.getId()));

        assertTrue(readingRepository.findAll().stream().anyMatch(r -> r.getUser().getId().equals(otherUser.getId())));
    }

    @Test
    void readingEndpointShouldRejectMissingToken() throws Exception {
        mockMvc.perform(get("/readings"))
                .andExpect(status().isUnauthorized());
    }
}
