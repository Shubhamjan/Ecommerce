package com.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(csrf -> csrf.disable())   // ✅ THIS FIXES YOUR ERROR
//            .authorizeExchange(exchange -> exchange
//                .pathMatchers("/api/users/login", "/api/users/register").permitAll()
//                .anyExchange().authenticated()
//            )
                .authorizeExchange(exchange -> exchange
                        .anyExchange().permitAll()
                )

                .build();
    }
}