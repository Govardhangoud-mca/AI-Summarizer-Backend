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
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper; 
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper; 
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.SessionFixationProtectionStrategy;

import com.summarizer_backend.service.UserDetailsServiceImpl;

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

    // CRITICAL FIX 1: Map roles without the default "ROLE_" prefix
    @Bean
    public GrantedAuthoritiesMapper grantedAuthoritiesMapper() {
        SimpleAuthorityMapper mapper = new SimpleAuthorityMapper();
        mapper.setPrefix(""); // Disable the "ROLE_" prefix
        mapper.setConvertToUpperCase(true); 
        return mapper;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        authProvider.setAuthoritiesMapper(grantedAuthoritiesMapper()); // Apply the mapper
        return authProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
             .cors(Customizer.withDefaults())
             .csrf(AbstractHttpConfigurer::disable)
             
             .authenticationProvider(authenticationProvider())
             
             .sessionManagement(session -> session
                 .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                 .sessionAuthenticationStrategy(new SessionFixationProtectionStrategy())
             )
             
             .formLogin(Customizer.withDefaults()) 
             
             .authorizeHttpRequests(auth -> auth
                 .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                 
              
                 .requestMatchers("/api/v1/auth/**").permitAll()
                 
                 .requestMatchers("/api/v1/text/**").hasAnyAuthority("USER", "ADMIN")
                 .requestMatchers("/api/v1/admin/**").hasAuthority("ADMIN")
                 
                 .anyRequest().authenticated()
             );

        return http.build();
    }
}