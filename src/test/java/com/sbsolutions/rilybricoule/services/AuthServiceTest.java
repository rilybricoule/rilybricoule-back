package com.sbsolutions.rilybricoule.services;

import com.sbsolutions.rilybricoule.dto.JwtResponse;
import com.sbsolutions.rilybricoule.dto.LoginRequest;
import com.sbsolutions.rilybricoule.dto.RegisterRequest;
import com.sbsolutions.rilybricoule.entity.*;
import com.sbsolutions.rilybricoule.exceptions.EmailAlreadyExistsException;
import com.sbsolutions.rilybricoule.exceptions.RefreshTokenExpiredException;
import com.sbsolutions.rilybricoule.exceptions.RefreshTokenNotFoundException;
import com.sbsolutions.rilybricoule.repository.RoleRepository;
import com.sbsolutions.rilybricoule.repository.UserRepository;
import com.sbsolutions.rilybricoule.security.application.AuthApplicationService;
import com.sbsolutions.rilybricoule.security.domain.model.RefreshToken;
import com.sbsolutions.rilybricoule.security.domain.port.out.AuditLogPort;
import com.sbsolutions.rilybricoule.security.domain.port.out.RefreshTokenRepositoryPort;
import com.sbsolutions.rilybricoule.security.domain.port.out.TokenProviderPort;
import com.sbsolutions.rilybricoule.services.OtpService;
import com.sbsolutions.rilybricoule.entity.OtpPurpose;
import com.sbsolutions.rilybricoule.security.infrastructure.oauth2.OAuth2TokenVerifierFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthApplicationService Unit Tests")
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private TokenProviderPort tokenProvider;
    @Mock private RefreshTokenRepositoryPort refreshTokenRepository;
    @Mock private AuditLogPort auditLog;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserDetailsService userDetailsService;
    @Mock private OtpService otpService;
    @Mock private OAuth2TokenVerifierFactory oAuth2TokenVerifierFactory;

    @InjectMocks
    private AuthApplicationService authService;

    private Role clientRole;
    private Role prestataireRole;

    @BeforeEach
    void setUp() {
        clientRole = new Role();
        clientRole.setId(1L);
        clientRole.setRoleName(RoleName.ROLE_CLIENT);

        prestataireRole = new Role();
        prestataireRole.setId(2L);
        prestataireRole.setRoleName(RoleName.ROLE_PRESTATAIRE);
    }

    @Nested
    @DisplayName("Register")
    class RegisterTests {

        @Test
        @DisplayName("Register CLIENT - success")
        void register_Client_Success() {
            RegisterRequest request = new RegisterRequest(
                    "Ahmed", "Benali", "ahmed@test.com", "password123",
                    "0612345678", "CLIENT", null, null, null
            );

            when(userRepository.existsByEmail("ahmed@test.com")).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
            when(roleRepository.findByRoleName(RoleName.ROLE_CLIENT)).thenReturn(Optional.of(clientRole));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                u.setId(1L);
                return u;
            });

            authService.register(request, "127.0.0.1", "TestAgent");

            verify(userRepository).save(any(Client.class));
            verify(passwordEncoder).encode("password123");
            verify(otpService).generateAndSendOtp("ahmed@test.com", OtpPurpose.EMAIL_VERIFICATION);
        }

        @Test
        @DisplayName("Register PRESTATAIRE - success")
        void register_Prestataire_Success() {
            RegisterRequest request = new RegisterRequest(
                    "Sara", "Moussaoui", "sara@test.com", "password123",
                    "0698765432", "PRESTATAIRE", "Sara Bricolage", "AB123456", "Expert plomberie"
            );

            when(userRepository.existsByEmail("sara@test.com")).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
            when(roleRepository.findByRoleName(RoleName.ROLE_PRESTATAIRE)).thenReturn(Optional.of(prestataireRole));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                u.setId(2L);
                return u;
            });

            authService.register(request, "127.0.0.1", "TestAgent");

            verify(userRepository).save(any(Prestataire.class));
            verify(otpService).generateAndSendOtp("sara@test.com", OtpPurpose.EMAIL_VERIFICATION);
        }

        @Test
        @DisplayName("Register - email already exists -> EmailAlreadyExistsException")
        void register_EmailAlreadyExists_ThrowsException() {
            RegisterRequest request = new RegisterRequest(
                    "Ahmed", "Benali", "existing@test.com", "password123",
                    "0612345678", "CLIENT", null, null, null
            );

            when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

            assertThrows(EmailAlreadyExistsException.class,
                    () -> authService.register(request, "127.0.0.1", "TestAgent"));

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Register - role not found -> RuntimeException")
        void register_RoleNotFound_ThrowsException() {
            RegisterRequest request = new RegisterRequest(
                    "Ahmed", "Benali", "ahmed@test.com", "password123",
                    "0612345678", "CLIENT", null, null, null
            );

            when(userRepository.existsByEmail("ahmed@test.com")).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encoded");
            when(roleRepository.findByRoleName(RoleName.ROLE_CLIENT)).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class,
                    () -> authService.register(request, "127.0.0.1", "TestAgent"));

            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Login")
    class LoginTests {

        @Test
        @DisplayName("Login - success")
        void login_Success() {
            LoginRequest request = new LoginRequest("ahmed@test.com", "password123");

            Authentication mockAuth = mock(Authentication.class);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(mockAuth);

            Client user = new Client();
            user.setId(1L);
            user.setEmail("ahmed@test.com");
            user.setFirstName("Ahmed");
            user.setLastName("Benali");
            user.setRoles(Set.of(clientRole));

            when(userRepository.findByEmail("ahmed@test.com")).thenReturn(Optional.of(user));

            UserDetails mockUserDetails = new org.springframework.security.core.userdetails.User(
                    "ahmed@test.com", "encodedPassword",
                    Set.of(new SimpleGrantedAuthority("ROLE_CLIENT"))
            );
            when(userDetailsService.loadUserByUsername("ahmed@test.com")).thenReturn(mockUserDetails);
            when(tokenProvider.generateAccessToken(mockUserDetails)).thenReturn("jwt-login-token");

            RefreshToken refreshToken = RefreshToken.builder()
                    .token("refresh-uuid-login")
                    .expiryDate(Instant.now().plusMillis(604800000))
                    .build();
            when(refreshTokenRepository.createRefreshToken(any(User.class))).thenReturn(refreshToken);

            JwtResponse response = authService.login(request, "127.0.0.1", "TestAgent");

            assertNotNull(response);
            assertEquals("jwt-login-token", response.getAccessToken());
            assertEquals("refresh-uuid-login", response.getRefreshToken());
            assertEquals("ahmed@test.com", response.getEmail());
            assertEquals("Ahmed", response.getFirstName());

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(refreshTokenRepository).revokeAllByUser(user);
            verify(auditLog).logLoginSuccess("ahmed@test.com", "127.0.0.1", "TestAgent");
        }

        @Test
        @DisplayName("Login - wrong password -> BadCredentialsException")
        void login_WrongPassword_ThrowsException() {
            LoginRequest request = new LoginRequest("ahmed@test.com", "wrongpassword");

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            BadCredentialsException ex = assertThrows(BadCredentialsException.class,
                    () -> authService.login(request, "127.0.0.1", "TestAgent"));

            assertEquals("Invalid email or password", ex.getMessage());
            verify(userRepository, never()).findByEmail(anyString());
            verify(auditLog).logLoginFailure(eq("ahmed@test.com"), eq("127.0.0.1"), eq("TestAgent"), anyString());
        }

        @Test
        @DisplayName("Login - non-existing email -> BadCredentialsException")
        void login_NonExistingEmail_ThrowsException() {
            LoginRequest request = new LoginRequest("noone@test.com", "password123");

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            assertThrows(BadCredentialsException.class,
                    () -> authService.login(request, "127.0.0.1", "TestAgent"));
        }
    }

    @Nested
    @DisplayName("Refresh Token")
    class RefreshTokenTests {

        @Test
        @DisplayName("Refresh - valid token -> new tokens")
        void refreshToken_ValidToken_Success() {
            Client user = new Client();
            user.setId(1L);
            user.setEmail("ahmed@test.com");
            user.setFirstName("Ahmed");
            user.setLastName("Benali");
            user.setRoles(Set.of(clientRole));

            RefreshToken existingToken = RefreshToken.builder()
                    .token("old-refresh-token")
                    .user(user)
                    .expiryDate(Instant.now().plusMillis(604800000))
                    .revoked(false)
                    .build();

            when(refreshTokenRepository.findByToken("old-refresh-token")).thenReturn(Optional.of(existingToken));

            UserDetails mockUserDetails = new org.springframework.security.core.userdetails.User(
                    "ahmed@test.com", "encodedPassword",
                    Set.of(new SimpleGrantedAuthority("ROLE_CLIENT"))
            );
            when(userDetailsService.loadUserByUsername("ahmed@test.com")).thenReturn(mockUserDetails);
            when(tokenProvider.generateAccessToken(mockUserDetails)).thenReturn("new-jwt-token");

            RefreshToken newRefreshToken = RefreshToken.builder()
                    .token("new-refresh-token")
                    .expiryDate(Instant.now().plusMillis(604800000))
                    .build();
            when(refreshTokenRepository.createRefreshToken(user)).thenReturn(newRefreshToken);

            JwtResponse response = authService.refreshToken("old-refresh-token", "127.0.0.1", "TestAgent");

            assertEquals("new-jwt-token", response.getAccessToken());
            assertEquals("new-refresh-token", response.getRefreshToken());
            verify(refreshTokenRepository).revokeToken(existingToken);
            verify(auditLog).logTokenRefresh("ahmed@test.com", "127.0.0.1", "TestAgent");
        }

        @Test
        @DisplayName("Refresh - token not found -> RefreshTokenNotFoundException")
        void refreshToken_NotFound_ThrowsException() {
            when(refreshTokenRepository.findByToken("nonexistent-token")).thenReturn(Optional.empty());

            assertThrows(RefreshTokenNotFoundException.class,
                    () -> authService.refreshToken("nonexistent-token", "127.0.0.1", "TestAgent"));
        }

        @Test
        @DisplayName("Refresh - expired token -> RefreshTokenExpiredException")
        void refreshToken_ExpiredToken_ThrowsException() {
            Client user = new Client();
            user.setId(1L);
            user.setEmail("ahmed@test.com");

            RefreshToken expiredToken = RefreshToken.builder()
                    .token("expired-refresh-token")
                    .user(user)
                    .expiryDate(Instant.now().minusMillis(1000))
                    .revoked(false)
                    .build();

            when(refreshTokenRepository.findByToken("expired-refresh-token")).thenReturn(Optional.of(expiredToken));

            assertThrows(RefreshTokenExpiredException.class,
                    () -> authService.refreshToken("expired-refresh-token", "127.0.0.1", "TestAgent"));

            verify(refreshTokenRepository).revokeToken(expiredToken);
        }
    }

    @Nested
    @DisplayName("Validate Token")
    class ValidateTokenTests {

        @Test
        @DisplayName("Validate - valid token -> true")
        void validateToken_Valid_ReturnsTrue() {
            when(tokenProvider.extractEmail("valid-token")).thenReturn("ahmed@test.com");

            UserDetails mockUserDetails = new org.springframework.security.core.userdetails.User(
                    "ahmed@test.com", "encodedPassword",
                    Set.of(new SimpleGrantedAuthority("ROLE_CLIENT"))
            );
            when(userDetailsService.loadUserByUsername("ahmed@test.com")).thenReturn(mockUserDetails);
            when(tokenProvider.isTokenValid("valid-token", mockUserDetails)).thenReturn(true);

            assertTrue(authService.validateToken("valid-token"));
        }

        @Test
        @DisplayName("Validate - invalid token -> false")
        void validateToken_Invalid_ReturnsFalse() {
            when(tokenProvider.extractEmail("bad-token")).thenReturn("ahmed@test.com");

            UserDetails mockUserDetails = new org.springframework.security.core.userdetails.User(
                    "ahmed@test.com", "encodedPassword",
                    Set.of(new SimpleGrantedAuthority("ROLE_CLIENT"))
            );
            when(userDetailsService.loadUserByUsername("ahmed@test.com")).thenReturn(mockUserDetails);
            when(tokenProvider.isTokenValid("bad-token", mockUserDetails)).thenReturn(false);

            assertFalse(authService.validateToken("bad-token"));
        }

        @Test
        @DisplayName("Validate - exception during validation -> false")
        void validateToken_Exception_ReturnsFalse() {
            when(tokenProvider.extractEmail("crash-token")).thenThrow(new RuntimeException("parse error"));

            assertFalse(authService.validateToken("crash-token"));
        }
    }
}
