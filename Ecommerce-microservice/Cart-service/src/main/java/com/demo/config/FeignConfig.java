package com.demo.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
@Slf4j
public class FeignConfig {



    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {

            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();

                String role = request.getHeader("X-User-Role");
                String id = request.getHeader("X-User-Id");

                if (role != null) {
                    requestTemplate.header("X-User-Role", role);
                }

                if (id != null) {
                    requestTemplate.header("X-User-Id", id);
                }
                log.info("Feign forwarding headers → userId={}, role={}", id, role);
            }


        };

    }

    @Bean
    public feign.Logger.Level feignLoggerLevel() {
        return feign.Logger.Level.FULL;
    }
}