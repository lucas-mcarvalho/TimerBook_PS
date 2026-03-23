package com.timerbook.TimerBook.services;

import com.timerbook.TimerBook.dto.FinishReadingDTO;
import com.timerbook.TimerBook.dto.InitReadingDTO;
import com.timerbook.TimerBook.models.Book;
import com.timerbook.TimerBook.models.Reading;
import com.timerbook.TimerBook.repository.BookRepository;
import com.timerbook.TimerBook.repository.ReadingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ReadingService {

    @Autowired
    private ReadingRepository readingRepository;

    @Autowired
    private BookRepository bookRepository;

    public Reading initializeReading(InitReadingDTO dto) {
        Optional<Book> book = bookRepository.findById(dto.getBookId());

        if (book.isEmpty()) {
            throw new IllegalArgumentException("Livro não encontrado");
        }

        Reading reading = new Reading();
        reading.setBook(book.get());
        reading.setCurrentPage(0);
        reading.setStartedAt(LocalDateTime.now());
        reading.setFinishedAt(null);

        return readingRepository.save(reading);
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
}