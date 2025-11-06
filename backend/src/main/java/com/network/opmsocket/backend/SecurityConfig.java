package com.network.opmsocket.backend;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public org.springframework.security.web.SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. Configure CORS
                .cors(Customizer.withDefaults()) // This will apply the 'corsConfigurationSource' bean

                // 2. Configure OAuth2 Resource Server (JWT validation)
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))

                // 3. Configure URL authorization
                .authorizeHttpRequests(authz -> authz
                        .anyRequest().authenticated() // All requests must be authenticated
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // This is the same configuration you had before
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        // This applies the configuration to your /api/** routes
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);

        return source;
    }
}