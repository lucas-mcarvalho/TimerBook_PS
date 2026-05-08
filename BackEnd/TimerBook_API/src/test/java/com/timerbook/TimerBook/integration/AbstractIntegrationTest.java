package com.timerbook.TimerBook.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.timerbook.TimerBook.models.Book;
import com.timerbook.TimerBook.models.Reading;
import com.timerbook.TimerBook.models.ReadingSession;
import com.timerbook.TimerBook.models.Role;
import com.timerbook.TimerBook.models.User;
import com.timerbook.TimerBook.repository.AchievementRepository;
import com.timerbook.TimerBook.repository.BookRepository;
import com.timerbook.TimerBook.repository.PasswordResetTokenRepository;
import com.timerbook.TimerBook.repository.ReadingRepository;
import com.timerbook.TimerBook.repository.ReadingSessionRepository;
import com.timerbook.TimerBook.repository.RoleRepository;
import com.timerbook.TimerBook.repository.UserAchievementRepository;
import com.timerbook.TimerBook.repository.UserRepository;
import com.timerbook.TimerBook.services.EmailService;
import com.timerbook.TimerBook.services.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@SpringBootTest(properties = {
        "api.security.token.secret=integration-test-secret",
        "spring.security.oauth2.client.registration.google.client-id=test-client",
        "spring.security.oauth2.client.registration.google.client-secret=test-secret",
        "app.reminders.reading.enabled=false"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
abstract class AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected RoleRepository roleRepository;

    @Autowired
    protected BookRepository bookRepository;

    @Autowired
    protected ReadingRepository readingRepository;

    @Autowired
    protected ReadingSessionRepository readingSessionRepository;

    @Autowired
    protected PasswordResetTokenRepository tokenRepository;

    @Autowired
    protected AchievementRepository achievementRepository;

    @Autowired
    protected UserAchievementRepository userAchievementRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected TokenService tokenService;

    @MockitoBean
    protected EmailService emailService;

    protected Role userRole;

    @BeforeEach
    void seedDefaultRole() {
        userRole = roleRepository.findByAuthority("ROLE_USER");
        if (userRole == null) {
            userRole = roleRepository.save(new Role(null, "ROLE_USER"));
        }
    }

    protected User createEnabledUser(String username, String email, String rawPassword) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setEnabled(true);
        user.getRoles().add(userRole);
        return userRepository.saveAndFlush(user);
    }

    protected String bearerFor(User user) {
        return "Bearer " + tokenService.generateToken(user);
    }

    protected Book createBook(User user, String name) {
        Book book = new Book();
        book.setName(name);
        book.setDescription("Descrição de " + name);
        book.setUser(user);
        return bookRepository.saveAndFlush(book);
    }

    protected Reading createReading(User user, Book book, Integer currentPage) {
        Reading reading = new Reading();
        reading.setUser(user);
        reading.setBook(book);
        reading.setCurrentPage(currentPage);
        reading.setStartedAt(LocalDateTime.of(2026, 5, 1, 10, 0));
        return readingRepository.saveAndFlush(reading);
    }

    protected ReadingSession createSession(Reading reading, LocalDateTime startedAt, LocalDateTime endedAt, Integer startPage, Integer endPage) {
        ReadingSession session = new ReadingSession();
        session.setReading(reading);
        session.setStartedAt(startedAt);
        session.setEndedAt(endedAt);
        session.setStartPage(startPage);
        session.setEndPage(endPage);
        return readingSessionRepository.saveAndFlush(session);
    }
}
