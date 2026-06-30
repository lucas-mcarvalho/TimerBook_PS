package com.timerbook.TimerBook.services;

import com.timerbook.TimerBook.models.User;
import com.timerbook.TimerBook.models.Role;
import com.timerbook.TimerBook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
        public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(login)
            .or(() -> userRepository.findByUsername(login))
            .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + login));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(user.getRoles().toArray(new Role[0]))
                .disabled(!user.getEnabled())
                .build();
    }
}