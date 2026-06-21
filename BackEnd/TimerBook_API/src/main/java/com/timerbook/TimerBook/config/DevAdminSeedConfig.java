package com.timerbook.TimerBook.config;

import com.timerbook.TimerBook.models.Role;
import com.timerbook.TimerBook.models.User;
import com.timerbook.TimerBook.repository.RoleRepository;
import com.timerbook.TimerBook.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("dev")
public class DevAdminSeedConfig {

    private static final Logger logger = LoggerFactory.getLogger(DevAdminSeedConfig.class);

    @Bean
    ApplicationRunner seedDevAdmin(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            @Value("${dev.admin.seed-enabled:false}") boolean seedEnabled,
            @Value("${dev.admin.email:admin@timerbook.local}") String adminEmail,
            @Value("${dev.admin.username:Admin}") String adminUsername,
            @Value("${dev.admin.password:Admin123!}") String adminPassword,
            @Value("${dev.admin.reset-password:false}") boolean resetPassword
    ) {
        return args -> {
            if (!seedEnabled) {
                return;
            }

            Role userRole = getOrCreateRole(roleRepository, "ROLE_USER");
            Role adminRole = getOrCreateRole(roleRepository, "ROLE_ADMIN");

            User admin = userRepository.findByEmail(adminEmail).orElseGet(User::new);
            boolean isNewUser = admin.getId() == null;

            admin.setEmail(adminEmail);
            admin.setUsername(adminUsername);
            admin.setEnabled(true);
            admin.setSubscriptionPlan("PAID");

            if (admin.getDailyReadingGoalMinutes() == null) {
                admin.setDailyReadingGoalMinutes(User.DEFAULT_DAILY_READING_GOAL_MINUTES);
            }

            if (isNewUser || resetPassword) {
                admin.setPassword(passwordEncoder.encode(adminPassword));
            }

            admin.getRoles().add(userRole);
            admin.getRoles().add(adminRole);

            userRepository.save(admin);
            logger.info("Usuario admin premium de desenvolvimento pronto: {}", adminEmail);
        };
    }

    private Role getOrCreateRole(RoleRepository roleRepository, String authority) {
        Role role = roleRepository.findByAuthority(authority);
        if (role != null) {
            return role;
        }

        return roleRepository.save(new Role(null, authority));
    }
}
