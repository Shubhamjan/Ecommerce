package com.demo.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBufferUtils;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Slf4j
@Configuration
public class RateLimiterConfig {

    // ─────────────────────────────────────────────────────
    // Login Rate Limiter Key Resolver
    // Combines IP + Username from request body
    // This prevents:
    // → Brute force attacks on a specific account
    // → One IP hammering multiple accounts
    // ────────────────────────────────────────────────────

    @Bean
    public KeyResolver loginKeyResolver(){

        return exchange -> exchange.getRequest()
                .getBody()
                .next()
                .flatMap(dataBuffer -> {
                    // ─────────────────────────────────────
                    // Read raw bytes from request body
                    // ─────────────────────────────────────
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);

                    String body = new String(bytes, StandardCharsets.UTF_8);

                    try{
                        // ─────────────────────────────────
                        // Parse JSON body to extract username
                        // Body: { "username": "john@gmail.com",
                        //         "password": "secret" }
                        // ─────────────────────────────────
                        ObjectMapper objectMapper = new ObjectMapper();
                        JsonNode jsonNode = objectMapper.readTree(body);

                        String username = jsonNode.has("username")
                                ? jsonNode.get("username").asText()
                                : "unknown";

                        // ─────────────────────────────────
                        // Get client IP address
                        // ─────────────────────────────────
                        String ipAddress = exchange.getRequest()
                                .getRemoteAddress()
                                .getAddress()
                                .getHostAddress();

                        log.debug("The request come from "+ipAddress);
                        // ─────────────────────────────────
                        // Combine IP + Username as Redis key
                        // Key format: "login:ip:username"
                        // Example: "login:192.168.1.1:john@gmail.com"
                        //
                        // This means:
                        // Same IP + Same username → same bucket
                        // Different IP or username → different bucket
                        // ─────────────────────────────────
                        String key = "login:" + ipAddress + ":" + username;

                        return Mono.just(key);

                    } catch (Exception e) {
//                        / if body parsing fails → fall back to IP only
                        String ipAddress = exchange.getRequest()
                                .getRemoteAddress()
                                .getAddress()
                                .getHostAddress();

                        return Mono.just("login:" + ipAddress);
                    }
                })
                .defaultIfEmpty("login:unknown");
    }
}
