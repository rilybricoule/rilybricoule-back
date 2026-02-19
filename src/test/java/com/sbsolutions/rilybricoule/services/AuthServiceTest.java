package com.sbsolutions.rilybricoule.services;

import com.sbsolutions.rilybricoule.dto.JwtResponse;
import com.sbsolutions.rilybricoule.dto.LoginRequest;
import com.sbsolutions.rilybricoule.dto.RegisterRequest;
import com.sbsolutions.rilybricoule.entity.*;
import com.sbsolutions.rilybricoule.exceptions.EmailAlreadyExistsException;
import com.sbsolutions.rilybricoule.exceptions.InvalidTokenException;
import com.sbsolutions.rilybricoule.repository.RoleRepository;
import com.sbsolutions.rilybricoule.repository.UserRepository;
import com.sbsolutions.rilybricoule.security.JwtService;
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
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserDetailsService userDetailsService;

    @InjectMocks
    private AuthService authService;

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

            UserDetails mockUserDetails = new org.springframework.security.core.userdetails.User(
                    "ahmed@test.com", "encodedPassword",
                    Set.of(new SimpleGrantedAuthority("ROLE_CLIENT"))
            );
            when(userDetailsService.loadUserByUsername("ahmed@test.com")).thenReturn(mockUserDetails);
            when(jwtService.generateToken(mockUserDetails)).thenReturn("jwt-token-123");

            JwtResponse response = authService.register(request);

            assertNotNull(response);
            assertEquals("jwt-token-123", response.getAccessToken());
            assertEquals("ahmed@test.com", response.getEmail());
            assertEquals("Ahmed", response.getFirstName());
            assertEquals("Benali", response.getLastName());
            assertTrue(response.getRoles().contains("ROLE_CLIENT"));

            verify(userRepository).save(any(Client.class));
            verify(passwordEncoder).encode("password123");
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

            UserDetails mockUserDetails = new org.springframework.security.core.userdetails.User(
                    "sara@test.com", "encodedPassword",
                    Set.of(new SimpleGrantedAuthority("ROLE_PRESTATAIRE"))
            );
            when(userDetailsService.loadUserByUsername("sara@test.com")).thenReturn(mockUserDetails);
            when(jwtService.generateToken(mockUserDetails)).thenReturn("jwt-token-456");

            JwtResponse response = authService.register(request);

            assertNotNull(response);
            assertEquals("jwt-token-456", response.getAccessToken());
            assertEquals("sara@test.com", response.getEmail());
            assertTrue(response.getRoles().contains("ROLE_PRESTATAIRE"));

            verify(userRepository).save(any(Prestataire.class));
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
                    () -> authService.register(request));

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
                    () -> authService.register(request));

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
            when(jwtService.generateToken(mockUserDetails)).thenReturn("jwt-login-token");

            JwtResponse response = authService.login(request);

            assertNotNull(response);
            assertEquals("jwt-login-token", response.getAccessToken());
            assertEquals("ahmed@test.com", response.getEmail());
            assertEquals("Ahmed", response.getFirstName());

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        }

        @Test
        @DisplayName("Login - wrong password -> BadCredentialsException")
        void login_WrongPassword_ThrowsException() {
            LoginRequest request = new LoginRequest("ahmed@test.com", "wrongpassword");

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            BadCredentialsException ex = assertThrows(BadCredentialsException.class,
                    () -> authService.login(request));

            assertEquals("Invalid email or password", ex.getMessage());
            verify(userRepository, never()).findByEmail(anyString());
        }

        @Test
        @DisplayName("Login - non-existing email -> BadCredentialsException")
        void login_NonExistingEmail_ThrowsException() {
            LoginRequest request = new LoginRequest("noone@test.com", "password123");

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            assertThrows(BadCredentialsException.class,
                    () -> authService.login(request));
        }
    }

    @Nested
    @DisplayName("Refresh Token")
    class RefreshTokenTests {

        @Test
        @DisplayName("Refresh - valid token -> new token")
        void refreshToken_ValidToken_Success() {
            String oldToken = "old-jwt-token";
            String authHeader = "Bearer " + oldToken;

            when(jwtService.extractEmail(oldToken)).thenReturn("ahmed@test.com");

            UserDetails mockUserDetails = new org.springframework.security.core.userdetails.User(
                    "ahmed@test.com", "encodedPassword",
                    Set.of(new SimpleGrantedAuthority("ROLE_CLIENT"))
            );
            when(userDetailsService.loadUserByUsername("ahmed@test.com")).thenReturn(mockUserDetails);
            when(jwtService.isTokenValid(oldToken, mockUserDetails)).thenReturn(true);
            when(jwtService.generateToken(mockUserDetails)).thenReturn("new-jwt-token");

            String newToken = authService.refreshToken(authHeader);

            assertEquals("new-jwt-token", newToken);
        }

        @Test
        @DisplayName("Refresh - invalid header -> InvalidTokenException")
        void refreshToken_InvalidHeader_ThrowsException() {
            assertThrows(InvalidTokenException.class,
                    () -> authService.refreshToken("InvalidHeader"));
        }

        @Test
        @DisplayName("Refresh - null header -> InvalidTokenException")
        void refreshToken_NullHeader_ThrowsException() {
            assertThrows(InvalidTokenException.class,
                    () -> authService.refreshToken(null));
        }

        @Test
        @DisplayName("Refresh - expired token -> InvalidTokenException")
        void refreshToken_ExpiredToken_ThrowsException() {
            String authHeader = "Bearer expired-token";

            when(jwtService.extractEmail("expired-token")).thenReturn("ahmed@test.com");

            UserDetails mockUserDetails = new org.springframework.security.core.userdetails.User(
                    "ahmed@test.com", "encodedPassword",
                    Set.of(new SimpleGrantedAuthority("ROLE_CLIENT"))
            );
            when(userDetailsService.loadUserByUsername("ahmed@test.com")).thenReturn(mockUserDetails);
            when(jwtService.isTokenValid("expired-token", mockUserDetails)).thenReturn(false);

            assertThrows(InvalidTokenException.class,
                    () -> authService.refreshToken(authHeader));
        }
    }

    @Nested
    @DisplayName("Validate Token")
    class ValidateTokenTests {

        @Test
        @DisplayName("Validate - valid token -> true")
        void validateToken_Valid_ReturnsTrue() {
            String authHeader = "Bearer valid-token";

            when(jwtService.extractEmail("valid-token")).thenReturn("ahmed@test.com");

            UserDetails mockUserDetails = new org.springframework.security.core.userdetails.User(
                    "ahmed@test.com", "encodedPassword",
                    Set.of(new SimpleGrantedAuthority("ROLE_CLIENT"))
            );
            when(userDetailsService.loadUserByUsername("ahmed@test.com")).thenReturn(mockUserDetails);
            when(jwtService.isTokenValid("valid-token", mockUserDetails)).thenReturn(true);

            assertTrue(authService.validateToken(authHeader));
        }

        @Test
        @DisplayName("Validate - invalid token -> false")
        void validateToken_Invalid_ReturnsFalse() {
            String authHeader = "Bearer bad-token";

            when(jwtService.extractEmail("bad-token")).thenReturn("ahmed@test.com");

            UserDetails mockUserDetails = new org.springframework.security.core.userdetails.User(
                    "ahmed@test.com", "encodedPassword",
                    Set.of(new SimpleGrantedAuthority("ROLE_CLIENT"))
            );
            when(userDetailsService.loadUserByUsername("ahmed@test.com")).thenReturn(mockUserDetails);
            when(jwtService.isTokenValid("bad-token", mockUserDetails)).thenReturn(false);

            assertFalse(authService.validateToken(authHeader));
        }

        @Test
        @DisplayName("Validate - no Bearer prefix -> false")
        void validateToken_NoBearerPrefix_ReturnsFalse() {
            assertFalse(authService.validateToken("just-a-token"));
        }

        @Test
        @DisplayName("Validate - null header -> false")
        void validateToken_NullHeader_ReturnsFalse() {
            assertFalse(authService.validateToken(null));
        }

        @Test
        @DisplayName("Validate - exception during validation -> false")
        void validateToken_Exception_ReturnsFalse() {
            String authHeader = "Bearer crash-token";

            when(jwtService.extractEmail("crash-token")).thenThrow(new RuntimeException("parse error"));

            assertFalse(authService.validateToken(authHeader));
        }
    }
}
