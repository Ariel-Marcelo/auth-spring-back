package com.example.authentication.infrastructure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity()
public class GlobalConfiguration {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(a -> a.requestMatchers(
                                        "/swagger-ui",          // <-- Ruta base sin barra
                                        "/swagger-ui/**",       // <-- Rutas internas (CSS, JS, etc.)
                                        "/swagger-ui.html",     // <-- Redirección opcional
                                        "/v3/api-docs",         // <-- JSON de OpenAPI sin barra
                                        "/v3/api-docs/**"       // <-- JSON de OpenAPI con barra
                                ).permitAll()
                        .anyRequest()
                        .authenticated()
                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}

