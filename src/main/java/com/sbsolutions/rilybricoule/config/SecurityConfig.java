package com.sbsolutions.rilybricoule.config;

import com.sbsolutions.rilybricoule.security.JwtAuthenticationEntryPoint;
import com.sbsolutions.rilybricoule.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationEntryPoint jwtAuthEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthEntryPoint))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // ──── Public endpoints ────
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()

                        // ──── Client endpoints ────
                        // Clients can read their own profile; ADMIN can manage all
                        .requestMatchers(HttpMethod.GET, "/api/clients/**").hasAnyRole("CLIENT", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/clients/**").hasAnyRole("CLIENT", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/clients/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/clients/**").hasRole("ADMIN")

                        // ──── Prestataire endpoints ────
                        // Prestataires can read/update their own profile; ADMIN can manage all
                        .requestMatchers(HttpMethod.GET, "/api/prestataires/**").hasAnyRole("PRESTATAIRE", "CLIENT", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/prestataires/**").hasAnyRole("PRESTATAIRE", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/prestataires/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/prestataires/**").hasRole("ADMIN")

                        // ──── Reservation endpoints ────
                        // Creating reservations: CLIENT only
                        .requestMatchers(HttpMethod.POST, "/api/reservations", "/api/reservations/with-payment").hasAnyRole("CLIENT", "ADMIN")
                        // Cancelling a reservation: CLIENT or ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/reservations/*/cancel").hasAnyRole("CLIENT", "ADMIN")
                        // Updating reservation status (accept/refuse): PRESTATAIRE or ADMIN
                        .requestMatchers(HttpMethod.PATCH, "/api/reservations/*/status").hasAnyRole("PRESTATAIRE", "ADMIN")
                        // Reading reservations: any authenticated user (service-layer enforces ownership)
                        .requestMatchers(HttpMethod.GET, "/api/reservations/**").authenticated()

                        // ──── Coupon endpoints ────
                        // Reading coupons: any authenticated user
                        .requestMatchers(HttpMethod.GET, "/api/coupons/**").authenticated()
                        // Managing coupons (create/update/delete): ADMIN only
                        .requestMatchers(HttpMethod.POST, "/api/coupons/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/coupons/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/coupons/**").hasRole("ADMIN")

                        // ──── Avis (reviews) endpoints ────
                        // Posting a review: CLIENT only
                        .requestMatchers(HttpMethod.POST, "/api/avis/**").hasAnyRole("CLIENT", "ADMIN")
                        // Reading reviews: any authenticated user
                        .requestMatchers(HttpMethod.GET, "/api/avis/**").authenticated()

                        // ──── Chat & Messages endpoints ────
                        // Both CLIENT and PRESTATAIRE can participate in chats
                        .requestMatchers("/api/chats/**").hasAnyRole("CLIENT", "PRESTATAIRE", "ADMIN")
                        .requestMatchers("/api/messages/**").hasAnyRole("CLIENT", "PRESTATAIRE", "ADMIN")

                        // ──── Notification endpoints ────
                        // Any authenticated user can manage their own notifications
                        .requestMatchers("/api/notifications/**").authenticated()

                        // ──── Default: deny unauthenticated ────
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
