package com.timerbook.TimerBook.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.timerbook.TimerBook.models.User;
import com.timerbook.TimerBook.repository.UserRepository;
import com.timerbook.TimerBook.services.TokenService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class SecurityFilter extends OncePerRequestFilter{
    
    @Autowired
    TokenService tokenService;

    @Autowired
    UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        var token = this.recoverToken(request);
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        var login = tokenService.validateToken(token);

        if (login != null) {
            User user = userRepository.findByEmail(login).orElseThrow(() -> new RuntimeException("User not found"));
            var authorities = user.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority(role.getAuthority()))
                    .toList();
            var authentication = new UsernamePasswordAuthenticationToken(user.getEmail(), null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }

    private String recoverToken(HttpServletRequest request) {
        var authHeader = request.getHeader("Authorization");
        if (authHeader == null || authHeader.isBlank() || !authHeader.startsWith("Bearer ")) return null;
        return authHeader.replace("Bearer ", "");
    }

    

    

}
