package com.timerbook.TimerBook.services;

import com.timerbook.TimerBook.models.Role;
import com.timerbook.TimerBook.models.User;
import com.timerbook.TimerBook.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomDetailsService service;

    @Test
    void loadUserByUsernameShouldReturnSpringUserDetails() {
        User user = new User();
        user.setEmail("reader@mail.com");
        user.setPassword("encoded");
        user.setEnabled(true);
        user.getRoles().add(new Role(1L, "ROLE_USER"));

        when(userRepository.findByEmail("reader@mail.com")).thenReturn(Optional.of(user));

        UserDetails result = service.loadUserByUsername("reader@mail.com");

        assertEquals("reader@mail.com", result.getUsername());
        assertEquals("encoded", result.getPassword());
        assertTrue(result.isEnabled());
        assertTrue(result.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void loadUserByUsernameShouldDisableSpringUserWhenDomainUserIsDisabled() {
        User user = new User();
        user.setEmail("reader@mail.com");
        user.setPassword("encoded");
        user.setEnabled(false);

        when(userRepository.findByEmail("reader@mail.com")).thenReturn(Optional.of(user));

        UserDetails result = service.loadUserByUsername("reader@mail.com");

        assertFalse(result.isEnabled());
    }

    @Test
    void loadUserByUsernameShouldThrowWhenUserDoesNotExist() {
        when(userRepository.findByEmail("missing@mail.com")).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> service.loadUserByUsername("missing@mail.com")
        );

        assertEquals("Usuário não encontrado: missing@mail.com", exception.getMessage());
    }
}
