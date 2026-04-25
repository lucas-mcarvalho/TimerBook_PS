package com.timerbook.TimerBook.services;

import com.timerbook.TimerBook.dto.AchievementDTO;
import com.timerbook.TimerBook.models.Achievement;
import com.timerbook.TimerBook.models.User;
import com.timerbook.TimerBook.models.UserAchievement;
import com.timerbook.TimerBook.repository.AchievementRepository;
import com.timerbook.TimerBook.repository.UserAchievementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AchievementService {

    @Autowired
    private AchievementRepository achievementRepo;

    @Autowired
    private UserAchievementRepository userAchievementRepo;

    public List<AchievementDTO> checkFirstLogin(User user) {
        List<AchievementDTO> conquistasDesbloqueadasAgora = new ArrayList<>();
        String keyCode = "FIRST_LOGIN";

        boolean jaPossui = userAchievementRepo.existsByUserAndAchievement_KeyCode(user, keyCode);

        if (!jaPossui) {
            Optional<Achievement> conquistaDb = achievementRepo.findByKeyCode(keyCode);

            if (conquistaDb.isPresent()) {
                Achievement achievement = conquistaDb.get();

                UserAchievement novoGanho = new UserAchievement(user, achievement);
                userAchievementRepo.save(novoGanho);

                conquistasDesbloqueadasAgora.add(
                        new AchievementDTO(achievement.getName(), achievement.getIconUrl())
                );
            }
        }

        return conquistasDesbloqueadasAgora;
    }

    public List<AchievementDTO> getUserMedals(Long userId) {
        List<UserAchievement> ganhos = userAchievementRepo.findByUserId(userId);

        return ganhos.stream()
                .map(ganho -> new AchievementDTO(
                        ganho.getAchievement().getName(),
                        ganho.getAchievement().getIconUrl()
                ))
                .toList();
    }
}