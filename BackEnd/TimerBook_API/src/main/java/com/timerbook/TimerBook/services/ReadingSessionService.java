package com.timerbook.TimerBook.services;

import com.timerbook.TimerBook.dto.StartReadingSessionDTO;
import com.timerbook.TimerBook.models.Reading;
import com.timerbook.TimerBook.models.ReadingSession;
import com.timerbook.TimerBook.repository.ReadingRepository;
import com.timerbook.TimerBook.repository.ReadingSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ReadingSessionService {
    @Autowired
    private ReadingSessionRepository readingSessionRepository;

    @Autowired
    private ReadingRepository readingRepository;

    public ReadingSession startReadingSession(StartReadingSessionDTO dto) {
        Optional<Reading> reading = readingRepository.findById(dto.getReadingId());

        if (reading.isEmpty()) {
            throw new IllegalArgumentException("Leitura não encontrada");
        }

        ReadingSession session = new ReadingSession();
        session.setReading(reading.get());
        session.setStartPage(dto.getStartPage());
        session.setEndPage(dto.getStartPage());
        session.setStartedAt(dto.getStartedAt() != null ? dto.getStartedAt() : LocalDateTime.now());

        return readingSessionRepository.save(session);
    }
}