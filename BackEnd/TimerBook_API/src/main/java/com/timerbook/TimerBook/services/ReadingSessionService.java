package com.timerbook.TimerBook.services;

import com.timerbook.TimerBook.dto.AchievementDTO;
import com.timerbook.TimerBook.dto.FinishSessionResponseDTO;
import com.timerbook.TimerBook.dto.StartReadingSessionDTO;
import com.timerbook.TimerBook.models.Reading;
import com.timerbook.TimerBook.models.ReadingSession;
import com.timerbook.TimerBook.repository.ReadingRepository;
import com.timerbook.TimerBook.repository.ReadingSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ReadingSessionService {
    @Autowired
    private ReadingSessionRepository readingSessionRepository;

    @Autowired
    private ReadingRepository readingRepository;
    @Autowired
    private AchievementService achievementService;

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

    public FinishSessionResponseDTO finishReadingSession(Long sessionId, Integer endPage) {
        Optional<ReadingSession> session = readingSessionRepository.findById(sessionId);
        if (session.isEmpty()) {
            throw new IllegalArgumentException("Sessão de leitura não encontrada");
        }

        ReadingSession existingSession = session.get();
        existingSession.setEndPage(endPage);
        existingSession.setEndedAt(LocalDateTime.now());

        ReadingSession savedSession = readingSessionRepository.save(existingSession);

        List<AchievementDTO> novas = new ArrayList<>();
        novas.addAll(achievementService.checkFirstSession(savedSession.getReading().getUser()));
        novas.addAll(achievementService.checkReadingStreak(savedSession.getReading().getUser()));

        return new FinishSessionResponseDTO(savedSession, novas);
    }


    public List<ReadingSession> getAll(){
            return  readingSessionRepository.findAll();
    }


    public ReadingSession getById(Long id) {
        return readingSessionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sessão não encontrada"));
    }

    public List<ReadingSession> getSessionsByReadingId(Long readingId) {
        if (!readingRepository.existsById(readingId)) {
            throw new IllegalArgumentException("Leitura não encontrada");
        }
        return readingSessionRepository.findByReadingId(readingId);
    }
}
