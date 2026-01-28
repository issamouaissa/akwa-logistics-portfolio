package org.sid.authentification.configs;

import org.sid.authentification.services.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    // This bean provides a password encoder that will be used to encode passwords.
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Return a new instance of BCryptPasswordEncoder, a strong password hashing algorithm.
        return new BCryptPasswordEncoder();
    }

    // dans une classe @Configuration
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }


    // This bean defines the security filter chain, which contains the security configurations.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    }

    // This bean provides an AuthenticationManager for managing authentication.
    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        // Get the shared AuthenticationManagerBuilder from the HttpSecurity context.
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);

        // Set the custom UserDetailsService and the password encoder for the authentication manager.
        authenticationManagerBuilder
                .userDetailsService(customUserDetailsService) // Use the custom UserDetailsService for user data retrieval.
                .passwordEncoder(passwordEncoder()); // Use the BCrypt password encoder for password matching.

        // Build and return the AuthenticationManager.
        return authenticationManagerBuilder.build();
    }
}

