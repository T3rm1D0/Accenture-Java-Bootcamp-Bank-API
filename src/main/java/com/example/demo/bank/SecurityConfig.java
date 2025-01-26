package com.example.demo.bank;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())  // Disable CSRF for development (not recommended for production)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/accounts/create").permitAll()  // Allow public access to create account
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()  // Allow Swagger access
                        .anyRequest().authenticated()  // Other endpoints require authentication
                )
                .httpBasic(withDefaults());  // Use withDefaults for basic authentication

        return http.build();
    }
}
