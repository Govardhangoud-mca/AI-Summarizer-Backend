package com.summarizer_backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.summarizer_backend.dto.RegisterRequest;
import com.summarizer_backend.model.User;
import com.summarizer_backend.model.User.Role;
import com.summarizer_backend.repository.UserRepository;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerNewUser(RegisterRequest request) {
        
        if (userRepository.existsByUsername(request.username())) {
            throw new RuntimeException("Username already taken.");
        }

        
        User newUser = new User();
        
        
        newUser.setUsername(request.username());
        
        
        newUser.setPassword(passwordEncoder.encode(request.password()));
        
        
        newUser.setRole(Role.USER); 

        return userRepository.save(newUser);
    }
    
    
}