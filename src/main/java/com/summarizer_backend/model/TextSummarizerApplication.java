package com.summarizer_backend.model;


import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.summarizer_backend.model.User.Role;
import com.summarizer_backend.repository.UserRepository;

@SpringBootApplication
public class TextSummarizerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TextSummarizerApplication.class, args);
    }

    
    @Bean
    public CommandLineRunner demoData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("adminpass")); 
                admin.setRole(Role.ADMIN);
                userRepository.save(admin);
                System.out.println("✅ Created ADMIN user: admin/adminpass");
            }

            if (userRepository.findByUsername("user").isEmpty()) {
                User regularUser = new User();
                regularUser.setUsername("user");
                regularUser.setPassword(passwordEncoder.encode("userpass")); 
                regularUser.setRole(Role.USER);
                userRepository.save(regularUser);
                System.out.println("✅ Created REGULAR user: user/userpass");
            }
        };
    }
}
