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
import static org.mockito.Mockito.*;

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
        User user = new User();
        user.setId(1L);
        user.setUsername("teste");
        user.setEmail("teste@gmail.com");
        user.setPassword("123456"); // 

        UserDTO newUser = new UserDTO();
        newUser.setUsername("teste2");
        newUser.setEmail("teste2@gmail.com");

        User userSalvo = new User();
        userSalvo.setId(1L);
        userSalvo.setUsername("teste2");
        userSalvo.setEmail("teste2@gmail.com");
        userSalvo.setPassword("123456"); 

        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(repository.save(any(User.class))).thenReturn(userSalvo);


       
        User result = services.update(1L, newUser);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("teste2", result.getUsername());
        assertEquals("teste2@gmail.com", result.getEmail()); 

        verify(repository, times(1)).findById(1L);
        verify(repository, times(1)).save(any(User.class));


    }

    @Test
    void delete() {
        User user = new User();
        Long id = 1L;
        user.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(user));
        services.delete(id);

        verify(repository,times(1)).findById(id);
        verify(repository,times(1)).delete(user);

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