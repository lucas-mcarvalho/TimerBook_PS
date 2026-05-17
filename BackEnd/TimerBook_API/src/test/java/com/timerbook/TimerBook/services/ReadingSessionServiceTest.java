package com.timerbook.TimerBook.services;

import com.timerbook.TimerBook.dto.AchievementDTO;
import com.timerbook.TimerBook.dto.FinishSessionResponseDTO;
import com.timerbook.TimerBook.dto.StartReadingSessionDTO;
import com.timerbook.TimerBook.models.Book;
import com.timerbook.TimerBook.models.Reading;
import com.timerbook.TimerBook.models.ReadingSession;
import com.timerbook.TimerBook.models.User;
import com.timerbook.TimerBook.repository.ReadingRepository;
import com.timerbook.TimerBook.repository.ReadingSessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReadingSessionServiceTest {

    @Mock
    private ReadingSessionRepository readingSessionRepository;

    @Mock
    private ReadingRepository readingRepository;

    @Mock
    private AchievementService achievementService;

    @InjectMocks
    private ReadingSessionService service;

    @Test
    void startReadingSessionShouldCreateSessionForReading() {
        Reading reading = reading();
        LocalDateTime startedAt = LocalDateTime.of(2024, 5, 8, 10, 0);
        StartReadingSessionDTO dto = new StartReadingSessionDTO(1L, 12, startedAt);
        ArgumentCaptor<ReadingSession> captor = ArgumentCaptor.forClass(ReadingSession.class);

        when(readingRepository.findById(1L)).thenReturn(Optional.of(reading));
        when(readingSessionRepository.save(any(ReadingSession.class))).thenAnswer(invocation -> {
            ReadingSession session = invocation.getArgument(0);
            session.setId(10L);
            return session;
        });

        ReadingSession result = service.startReadingSession(dto);

        assertEquals(10L, result.getId());
        verify(readingSessionRepository).save(captor.capture());
        ReadingSession saved = captor.getValue();
        assertEquals(reading, saved.getReading());
        assertEquals(12, saved.getStartPage());
        assertEquals(12, saved.getEndPage());
        assertEquals(startedAt, saved.getStartedAt());
        assertNull(saved.getEndedAt());
    }

    @Test
    void startReadingSessionShouldUseNowWhenStartedAtIsMissing() {
        Reading reading = reading();
        StartReadingSessionDTO dto = new StartReadingSessionDTO(1L, 12, null);

        when(readingRepository.findById(1L)).thenReturn(Optional.of(reading));
        when(readingSessionRepository.save(any(ReadingSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReadingSession result = service.startReadingSession(dto);

        assertNotNull(result.getStartedAt());
    }

    @Test
    void startReadingSessionShouldRejectMissingReading() {
        when(readingRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.startReadingSession(new StartReadingSessionDTO(1L, 12, null))
        );

        assertEquals("Leitura não encontrada", exception.getMessage());
    }

    @Test
    void finishReadingSessionShouldUpdateEndPageEndedAtAndReturnAchievements() {
        ReadingSession session = session(10L);
        AchievementDTO firstSessionAchievement = new AchievementDTO("Primeiro livro", "book.svg", "Descrição");
        AchievementDTO streakAchievement = new AchievementDTO("Sequência de 1 dia", "fire.svg", "Descrição streak");

        when(readingSessionRepository.findById(10L)).thenReturn(Optional.of(session));
        when(readingSessionRepository.save(session)).thenReturn(session);
        when(achievementService.checkFirstSession(session.getReading().getUser()))
                .thenReturn(List.of(firstSessionAchievement));
        when(achievementService.checkReadingStreak(session.getReading().getUser()))
                .thenReturn(List.of(streakAchievement));

        FinishSessionResponseDTO response = service.finishReadingSession(10L, 30);

        assertEquals(session, response.session());
        assertEquals(List.of(firstSessionAchievement, streakAchievement), response.novasConquistas());
        assertEquals(30, session.getEndPage());
        assertNotNull(session.getEndedAt());
        verify(readingSessionRepository).save(session);
    }

    @Test
    void finishReadingSessionShouldRejectMissingSession() {
        when(readingSessionRepository.findById(10L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.finishReadingSession(10L, 30)
        );

        assertEquals("Sessão de leitura não encontrada", exception.getMessage());
    }

    @Test
    void getAllShouldDelegateToRepository() {
        ReadingSession session = session(10L);
        when(readingSessionRepository.findAll()).thenReturn(List.of(session));

        assertEquals(List.of(session), service.getAll());
    }

    @Test
    void getByIdShouldReturnSession() {
        ReadingSession session = session(10L);
        when(readingSessionRepository.findById(10L)).thenReturn(Optional.of(session));

        assertEquals(session, service.getById(10L));
    }

    @Test
    void getByIdShouldThrowWhenMissing() {
        when(readingSessionRepository.findById(10L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> service.getById(10L));

        assertEquals("Sessão não encontrada", exception.getMessage());
    }

    @Test
    void getSessionsByReadingIdShouldReturnSessionsWhenReadingExists() {
        ReadingSession session = session(10L);
        when(readingRepository.existsById(1L)).thenReturn(true);
        when(readingSessionRepository.findByReadingId(1L)).thenReturn(List.of(session));

        assertEquals(List.of(session), service.getSessionsByReadingId(1L));
    }

    @Test
    void getSessionsByReadingIdShouldRejectMissingReading() {
        when(readingRepository.existsById(1L)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.getSessionsByReadingId(1L)
        );

        assertEquals("Leitura não encontrada", exception.getMessage());
    }

    private ReadingSession session(Long id) {
        ReadingSession session = new ReadingSession();
        session.setId(id);
        session.setReading(reading());
        session.setStartPage(10);
        session.setEndPage(10);
        session.setStartedAt(LocalDateTime.of(2024, 5, 8, 10, 0));
        return session;
    }

    private Reading reading() {
        User user = new User();
        user.setId(1L);
        user.setUsername("reader");

        Book book = new Book();
        book.setId(1L);
        book.setName("Livro");

        Reading reading = new Reading();
        reading.setId(1L);
        reading.setUser(user);
        reading.setBook(book);
        reading.setCurrentPage(10);
        return reading;
    }
}
