package com.demo.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class HeaderAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws IOException, ServletException {

        String userId = request.getHeader("X-User-Id");
        String role   = request.getHeader("X-User-Role");
        log.info("HeaderAuthFilter → userId={}, role={}", userId, role);

        if (userId != null && role != null) {
            List<GrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority(role));

            UsernamePasswordAuthenticationToken auth1 =
                new UsernamePasswordAuthenticationToken(userId, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(auth1);
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            log.info("Auth object: {}", auth);
            log.info("Is authenticated: {}", auth != null && auth.isAuthenticated());
            log.info("Authorities: {}", auth != null ? auth.getAuthorities() : null);
            log.info("Authentication set for userId={}", userId);  // ADD THIS
        } else {
            log.warn("Missing headers — userId={}, role={}", userId, role);  // ADD THIS
        }

        filterChain.doFilter(request, response);

        //removed after

        // ADD THIS ↓ — check if auth survived
        Authentication authAfter = SecurityContextHolder.getContext().getAuthentication();
        log.info(">>> Auth AFTER filterChain.doFilter = {}", authAfter);
    }
}