package com.sunil.ai.claims.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sunil.ai.claims.config.RateLimiterConfig;
import com.sunil.ai.claims.exception.ErrorResponse;
import com.sunil.ai.claims.security.util.ClaimsPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimiterConfig rateLimiterConfig;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder
                .getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof ClaimsPrincipal principal)) {
            filterChain.doFilter(request, response);
            return;
        }

        Long userId = principal.getUserId();

        if (!rateLimiterConfig.isAllowed(userId)) {
            log.warn("Rate limit exceeded for userId: {}", userId);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            ErrorResponse error = ErrorResponse.builder()
                    .status(HttpStatus.TOO_MANY_REQUESTS.value())
                    .message("Too many requests. Please try again later.")
                    .timestamp(LocalDateTime.now())
                    .build();
            response.getWriter().write(objectMapper.writeValueAsString(error));
            return;
        }

        filterChain.doFilter(request, response);
    }
}