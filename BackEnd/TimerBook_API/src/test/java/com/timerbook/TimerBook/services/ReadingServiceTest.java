package com.timerbook.TimerBook.services;

import com.timerbook.TimerBook.dto.FinishReadingDTO;
import com.timerbook.TimerBook.dto.InitReadingDTO;
import com.timerbook.TimerBook.models.Book;
import com.timerbook.TimerBook.models.Reading;
import com.timerbook.TimerBook.models.ReadingSession;
import com.timerbook.TimerBook.models.User;
import com.timerbook.TimerBook.repository.BookRepository;
import com.timerbook.TimerBook.repository.ReadingRepository;
import com.timerbook.TimerBook.repository.ReadingSessionRepository;
import com.timerbook.TimerBook.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReadingServiceTest {

    @Mock
    private ReadingRepository readingRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReadingSessionRepository readingSessionRepository;

    @InjectMocks
    private ReadingService service;

    @Test
    void initializeReadingShouldCreateReadingAndInitialSessionWhenNoActiveReadingExists() {
        User user = user(1L);
        Book book = book(10L);
        InitReadingDTO dto = new InitReadingDTO(10L, null);
        ArgumentCaptor<ReadingSession> sessionCaptor = ArgumentCaptor.forClass(ReadingSession.class);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));
        when(readingRepository.findByBookIdAndUserIdAndFinishedAtIsNull(10L, 1L)).thenReturn(Optional.empty());
        when(readingRepository.save(any(Reading.class))).thenAnswer(invocation -> {
            Reading reading = invocation.getArgument(0);
            reading.setId(100L);
            return reading;
        });

        Reading result = service.initializeReading(1L, dto);

        assertEquals(100L, result.getId());
        assertEquals(book, result.getBook());
        assertEquals(user, result.getUser());
        assertEquals(0, result.getCurrentPage());
        assertNotNull(result.getStartedAt());
        assertNull(result.getFinishedAt());
        verify(readingSessionRepository).save(sessionCaptor.capture());
        ReadingSession session = sessionCaptor.getValue();
        assertEquals(result, session.getReading());
        assertEquals(0, session.getStartPage());
        assertEquals(0, session.getEndPage());
        assertNotNull(session.getStartedAt());
        assertNull(session.getEndedAt());
    }

    @Test
    void initializeReadingShouldUpdateActiveReadingAndCreateNewSession() {
        User user = user(1L);
        Book book = book(10L);
        Reading activeReading = reading(100L, user, book);
        activeReading.setCurrentPage(3);
        InitReadingDTO dto = new InitReadingDTO(10L, 12);
        ArgumentCaptor<ReadingSession> sessionCaptor = ArgumentCaptor.forClass(ReadingSession.class);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));
        when(readingRepository.findByBookIdAndUserIdAndFinishedAtIsNull(10L, 1L)).thenReturn(Optional.of(activeReading));
        when(readingRepository.save(activeReading)).thenReturn(activeReading);

        Reading result = service.initializeReading(1L, dto);

        assertSame(activeReading, result);
        assertEquals(12, result.getCurrentPage());
        verify(readingSessionRepository).save(sessionCaptor.capture());
        assertEquals(12, sessionCaptor.getValue().getStartPage());
        assertEquals(12, sessionCaptor.getValue().getEndPage());
    }

    @Test
    void initializeReadingShouldRejectMissingUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.initializeReading(1L, new InitReadingDTO(10L, 0))
        );

        assertEquals("Usuário não encontrado", exception.getMessage());
    }

    @Test
    void initializeReadingShouldRejectMissingBook() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(bookRepository.findById(10L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.initializeReading(1L, new InitReadingDTO(10L, 0))
        );

        assertEquals("Livro não encontrado", exception.getMessage());
    }

    @Test
    void finishReadingShouldUpdateFinalPageAndFinishedAt() {
        User user = user(1L);
        Reading reading = reading(100L, user, book(10L));

        when(readingRepository.findById(100L)).thenReturn(Optional.of(reading));
        when(readingRepository.save(any(Reading.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Reading result = service.finishReading(1L, 100L, new FinishReadingDTO(45, "fim"));

        assertEquals(45, result.getCurrentPage());
        assertNotNull(result.getFinishedAt());
        verify(readingRepository).save(reading);
    }

    @Test
    void finishReadingShouldKeepCurrentPageWhenFinalPageIsNull() {
        User user = user(1L);
        Reading reading = reading(100L, user, book(10L));
        reading.setCurrentPage(20);

        when(readingRepository.findById(100L)).thenReturn(Optional.of(reading));
        when(readingRepository.save(any(Reading.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Reading result = service.finishReading(1L, 100L, new FinishReadingDTO(null, null));

        assertEquals(20, result.getCurrentPage());
        assertNotNull(result.getFinishedAt());
    }

    @Test
    void finishReadingShouldRejectUnauthorizedUser() {
        Reading reading = reading(100L, user(2L), book(10L));
        when(readingRepository.findById(100L)).thenReturn(Optional.of(reading));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.finishReading(1L, 100L, new FinishReadingDTO(45, null))
        );

        assertEquals("Você não tem permissão para finalizar esta leitura", exception.getMessage());
        verify(readingRepository, never()).save(any());
    }

    @Test
    void getReadingByIdShouldReturnReading() {
        Reading reading = reading(100L, user(1L), book(10L));
        when(readingRepository.findById(100L)).thenReturn(Optional.of(reading));

        assertEquals(reading, service.getReadingById(100L));
    }

    @Test
    void getReadingByIdShouldThrowWhenMissing() {
        when(readingRepository.findById(100L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> service.getReadingById(100L));

        assertEquals("Leitura não encontrada", exception.getMessage());
    }

    @Test
    void getReadingsByBookIdShouldReturnUserReadingsWhenBookExists() {
        Reading reading = reading(100L, user(1L), book(10L));
        when(bookRepository.existsById(10L)).thenReturn(true);
        when(readingRepository.findByBookIdAndUserId(10L, 1L)).thenReturn(List.of(reading));

        List<Reading> result = service.getReadingsByBookId(10L, 1L);

        assertEquals(List.of(reading), result);
    }

    @Test
    void getReadingsByBookIdShouldRejectMissingBook() {
        when(bookRepository.existsById(10L)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.getReadingsByBookId(10L, 1L)
        );

        assertEquals("Livro não encontrado", exception.getMessage());
    }

    @Test
    void getAllShouldDelegateToRepository() {
        Reading reading = reading(100L, user(1L), book(10L));
        when(readingRepository.findAll()).thenReturn(List.of(reading));

        assertEquals(List.of(reading), service.getAll());
    }

    private User user(Long id) {
        User user = new User();
        user.setId(id);
        user.setUsername("reader-" + id);
        return user;
    }

    private Book book(Long id) {
        Book book = new Book();
        book.setId(id);
        book.setName("Livro");
        return book;
    }

    private Reading reading(Long id, User user, Book book) {
        Reading reading = new Reading();
        reading.setId(id);
        reading.setUser(user);
        reading.setBook(book);
        reading.setCurrentPage(0);
        return reading;
    }
}
