package com.summarizer_backend.service;

import com.summarizer_backend.dto.LoginRequest;
import com.summarizer_backend.dto.LoginResponse;
import com.summarizer_backend.dto.RegisterRequest;
import com.summarizer_backend.model.User;
import com.summarizer_backend.model.User.Role;
import com.summarizer_backend.repository.UserRepository;
import com.summarizer_backend.config.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    // ✅ Register new user or admin based on request
    public User registerNewUser(RegisterRequest request) {

        if (userRepository.existsByUsername(request.username())) {
            throw new RuntimeException("Username already exists");
        }

        User newUser = new User();
        newUser.setUsername(request.username());
        newUser.setPassword(passwordEncoder.encode(request.password()));

        // ✅ Set role from request (default to USER if null or invalid)
        if (request.role() != null && request.role().equalsIgnoreCase("ADMIN")) {
            newUser.setRole(Role.ADMIN);
        } else {
            newUser.setRole(Role.USER);
        }

        return userRepository.save(newUser);
    }

    // ✅ Login and generate JWT token
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtil.generateJwtToken(authentication);

        return new LoginResponse(
                token,
                "Bearer",
                user.getId(),
                user.getUsername(),
                user.getRole().name() // ✅ Converts ENUM to string (USER / ADMIN)
        );
    }
}
