package com.timerbook.TimerBook.services;

import com.timerbook.TimerBook.dto.UserDTO;
import com.timerbook.TimerBook.models.User;
import com.timerbook.TimerBook.repository.UserRepository;
import com.timerbook.TimerBook.unittests.mapper.MockUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
class UserServiceTest {


    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    UserRepository repository;

    @InjectMocks
    private UserService services;
    @Test
    void create() {
        UserDTO user = new UserDTO();
        user.setUsername("teste");
        user.setEmail("teste@gmail.com");
        user.setPassword("123456");

        User usersave = new User();
        usersave.setId(1L);
        usersave.setUsername("teste");
        usersave.setEmail("teste@gmail.com");
        usersave.setPassword("123");

        when(passwordEncoder.encode(anyString())).thenReturn("123");
        when(repository.save(any(User.class))).thenReturn(usersave);

        User result = services.create(user);

        assertNotNull(result);
        assertEquals(1L,result.getId());
        assertEquals("teste",result.getUsername());
        assertEquals("teste@gmail.com",result.getEmail());
        assertEquals("123",result.getPassword());

    }

    @Test
    void update() {
    }

    @Test
    void delete() {
    }

    @Test
    void findById() {
        User user = new User();
        user.setId(1L);
        user.setUsername("teste");
        user.setEmail("teste@gmail.com");

        when(repository.findById(1L)).thenReturn(Optional.of(user));
        User user2 = services.findById(1L);

        assertNotNull(user2);
        assertEquals(1L,user2.getId());
        assertEquals("teste",user2.getUsername());

    }
}