package com.summarizer_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.summarizer_backend.service.UserDetailsServiceImpl;
import java.util.stream.Collectors; // Needed for the AuthoritiesMapper

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ✅ FIX: Replaced SimpleAuthorityMapper with modern Lambda/Stream implementation.
    // This removes the "ROLE_" prefix and converts to uppercase for authority checks.
    @Bean
    public GrantedAuthoritiesMapper grantedAuthoritiesMapper() {
        return (authorities) -> authorities.stream()
            .map(authority -> {
                String role = authority.getAuthority();
                // Remove the "ROLE_" prefix if it exists, and convert to uppercase
                if (role.startsWith("ROLE_")) {
                    role = role.substring(5); // "ROLE_".length() == 5
                }
                return new SimpleGrantedAuthority(role.toUpperCase());
            })
            .collect(Collectors.toSet());
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        authProvider.setAuthoritiesMapper(grantedAuthoritiesMapper()); // Apply the new mapper
        return authProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(AbstractHttpConfigurer::disable)
            
            // ✅ FIX: Disable the HTML form login page to prevent the redirect error (401 -> 302 -> /login)
            .formLogin(AbstractHttpConfigurer::disable) 
            
            // ✅ FIX: Set session management to STATELESS for JWT-based APIs
            // Also removed the deprecated SessionFixationProtectionStrategy which is for stateful sessions
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) 
            )
            
            .authenticationProvider(authenticationProvider())
            
            .authorizeHttpRequests(auth -> auth
                // Allow preflight requests (OPTIONS) for CORS
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                
                // Allow registration and login endpoints without authentication
                .requestMatchers("/api/v1/auth/**").permitAll()
                
                // Protected endpoints: Summarization API requires USER or ADMIN
                .requestMatchers("/api/v1/text/**").hasAnyAuthority("USER", "ADMIN")
                
                // Protected endpoints: Admin API requires ADMIN
                .requestMatchers("/api/v1/admin/**").hasAuthority("ADMIN")
                
                // All other requests must be authenticated (require a valid JWT)
                .anyRequest().authenticated()
            );

        // NOTE: If you have a JwtAuthenticationFilter, it needs to be added here 
        // using .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
        
        return http.build();
    }
}