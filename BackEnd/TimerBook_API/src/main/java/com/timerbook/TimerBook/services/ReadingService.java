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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReadingService {

    @Autowired
    private ReadingRepository readingRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReadingSessionRepository readingSessionRepository;

    @Autowired
    private AchievementService achievementService;

    public Reading initializeReading(Long userId, InitReadingDTO dto) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        Optional<Book> book = bookRepository.findById(dto.getBookId());
        if (book.isEmpty()) {
            throw new IllegalArgumentException("Livro não encontrado");
        }

        Integer startPage = dto.getStartPage() != null ? dto.getStartPage() : 0;

        Optional<Reading> activeReadingOpt = readingRepository
                .findByBookIdAndUserIdAndFinishedAtIsNull(dto.getBookId(), userId);

        Reading reading;

        if (activeReadingOpt.isPresent()) {
            reading = activeReadingOpt.get();
            reading.setCurrentPage(startPage);
            reading = readingRepository.save(reading);
        } else {
            reading = new Reading();
            reading.setBook(book.get());
            reading.setUser(user);
            reading.setCurrentPage(startPage);
            reading.setStartedAt(LocalDateTime.now());
            reading.setFinishedAt(null);
            reading = readingRepository.save(reading);
        }

        ReadingSession session = new ReadingSession();
        session.setReading(reading);
        session.setStartPage(startPage);
        session.setEndPage(startPage);
        session.setStartedAt(LocalDateTime.now());
        session.setEndedAt(null);

        readingSessionRepository.save(session);

        return reading;
    }

    public Reading finishReading(Long userId, Long readingId, FinishReadingDTO dto) {
        Reading readingEntity = readingRepository.findById(readingId)
                .orElseThrow(() -> new IllegalArgumentException("Leitura não encontrada"));

        if (!readingEntity.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Você não tem permissão para finalizar esta leitura");
        }

        if (dto.getFinalPage() != null) {
            readingEntity.setCurrentPage(dto.getFinalPage());
        }

        readingEntity.setFinishedAt(LocalDateTime.now());
        Reading savedReading = readingRepository.save(readingEntity);
        achievementService.checkFastBookRead(savedReading);
        return savedReading;
    }

    public Reading getReadingById(Long readingId) {
        return readingRepository.findById(readingId)
                .orElseThrow(() -> new IllegalArgumentException("Leitura não encontrada"));
    }

    public List<Reading> getReadingsByBookId(Long bookId, Long userId) {
        if (!bookRepository.existsById(bookId)) {
            throw new IllegalArgumentException("Livro não encontrado");
        }

        return readingRepository.findByBookIdAndUserId(bookId, userId);
    }

    public List<Reading> getAll() {
        return readingRepository.findAll();
    }
}
