package com.triplan.demo.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.triplan.demo.dto.AuthResponse;
import com.triplan.demo.dto.LoginRequest;
import com.triplan.demo.dto.RegisterRequest;
import com.triplan.demo.model.User;
import com.triplan.demo.repository.UserRepository;
import com.triplan.demo.security.JwtUtil;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final CacheService cacheService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       CacheService cacheService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.cacheService = cacheService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail());

        // Кешуємо користувача та сесію
        cacheService.cacheUser(user.getEmail(), user);
        cacheService.saveSession(user.getEmail(), token);

        return new AuthResponse(token, user.getEmail(), user.getFirstName(), user.getLastName());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(user.getEmail());

        // Кешуємо користувача та сесію
        cacheService.cacheUser(user.getEmail(), user);
        cacheService.saveSession(user.getEmail(), token);

        return new AuthResponse(token, user.getEmail(), user.getFirstName(), user.getLastName());
    }

    public void logout(String email) {
        // Видаляємо сесію з кешу
        cacheService.invalidateSession(email);
        cacheService.evictUser(email);
    }
}
