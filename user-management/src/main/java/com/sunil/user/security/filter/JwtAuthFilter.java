package com.sunil.user.security.filter;

import com.sunil.user.security.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {

        //Redd Authorization header from incoming request
        String authHeader = request.getHeader("Authorization");

        // If no token present, skip JWT processing and continue filter chain
        // This handles public endpoints like /register and /login
        if (authHeader == null || !authHeader.startsWith("Bearer")) {
            doFilter(request, response, filterChain);
            return;
        }

        // Remove "Bearer " prefix to get raw token string
        String token = authHeader.substring(7);

        // Validate token - checks signature, expiry and format
        if (jwtUtils.validateToken(token)) {

            // Extract username and role from token payload
            String username = jwtUtils.extractUsername(token);
            String role = jwtUtils.extractRole(token);
            Long userId = jwtUtils.extractUserId(token);

            // Create Spring Security authentication object
            // null for credentials - token already verified, no need for password
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            List.of(new SimpleGrantedAuthority(role))
                    );

            // Store authentication in SecurityContext
            // Spring Security now knows who is making this request
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

        // Pass request to next filter or controller
        // Must always be called at the end
        filterChain.doFilter(request, response);
    }
}
