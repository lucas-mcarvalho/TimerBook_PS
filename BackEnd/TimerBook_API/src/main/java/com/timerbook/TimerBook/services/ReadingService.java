package com.timerbook.TimerBook.services;

import com.timerbook.TimerBook.dto.FinishReadingDTO;
import com.timerbook.TimerBook.dto.InitReadingDTO;
import com.timerbook.TimerBook.models.Book;
import com.timerbook.TimerBook.models.Reading;
import com.timerbook.TimerBook.models.ReadingSession;
import com.timerbook.TimerBook.repository.BookRepository;
import com.timerbook.TimerBook.repository.ReadingRepository;
import com.timerbook.TimerBook.repository.ReadingSessionRepository;
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
    private ReadingSessionRepository readingSessionRepository;

    public Reading initializeReading(InitReadingDTO dto) {
        Optional<Book> book = bookRepository.findById(dto.getBookId());
        if (book.isEmpty()) {
            throw new IllegalArgumentException("Livro não encontrado");
        }

        Integer startPage = dto.getStartPage() != null ? dto.getStartPage() : 0;

        Optional<Reading> activeReadingOpt = readingRepository.findByBookIdAndFinishedAtIsNull(dto.getBookId());

        Reading reading;

        if (activeReadingOpt.isPresent()) {
            reading = activeReadingOpt.get();
            reading.setCurrentPage(startPage);
            reading = readingRepository.save(reading);
        } else {
            reading = new Reading();
            reading.setBook(book.get());
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
    public Reading finishReading(Long readingId, FinishReadingDTO dto) {
        Optional<Reading> reading = readingRepository.findById(readingId);
        if (reading.isEmpty()) {
            throw new IllegalArgumentException("Leitura não encontrada");
        }

        Reading readingEntity = reading.get();
        if (dto.getFinalPage() != null) {
            readingEntity.setCurrentPage(dto.getFinalPage());
        }
        readingEntity.setFinishedAt(LocalDateTime.now());
        return readingRepository.save(readingEntity);
    }

    public Reading getReadingById(Long readingId) {
        Optional<Reading> reading = readingRepository.findById(readingId);
        if (reading.isEmpty()) {
            throw new IllegalArgumentException("Leitura não encontrada");
        }
        return reading.get();
    }

    public List<Reading> getReadingsByBookId(Long bookId) {
        if (!bookRepository.existsById(bookId)) {
            throw new IllegalArgumentException("Livro não encontrado");
        }
        return readingRepository.findByBookId(bookId);
    }

    public Reading getById(Long id) {
        return readingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Leitura não encontrada"));
    }

    public List<Reading> getAll() {
        return readingRepository.findAll();
    }
}