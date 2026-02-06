package com.sbsolutions.rilybricoule.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getServletPath().startsWith("/api/auth");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = header.substring(7);
            Claims claims = jwtService.extractAllClaims(token);

            if (claims.getExpiration().before(new Date())) {
                filterChain.doFilter(request, response);
                return;
            }

            List<GrantedAuthority> authorities = new ArrayList<>();

            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) claims.get("roles");
            if (roles != null)
                roles.forEach(r -> authorities.add(new SimpleGrantedAuthority(r)));

            @SuppressWarnings("unchecked")
            List<String> permissions = (List<String>) claims.get("permissions");
            if (permissions != null)
                permissions.forEach(p -> authorities.add(new SimpleGrantedAuthority(p)));

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            claims.getSubject(),
                            null,
                            authorities
                    );

            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (Exception e) {
            // Log or handle JWT validation errors
            filterChain.doFilter(request, response);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
