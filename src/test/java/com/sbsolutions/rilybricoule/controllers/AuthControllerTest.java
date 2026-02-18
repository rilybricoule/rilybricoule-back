package com.sbsolutions.rilybricoule.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sbsolutions.rilybricoule.dto.JwtResponse;
import com.sbsolutions.rilybricoule.dto.LoginRequest;
import com.sbsolutions.rilybricoule.dto.RegisterRequest;
import com.sbsolutions.rilybricoule.exceptions.EmailAlreadyExistsException;
import com.sbsolutions.rilybricoule.security.CustomUserDetailsService;
import com.sbsolutions.rilybricoule.security.JwtService;
import com.sbsolutions.rilybricoule.services.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private AuthService authService;
    @MockBean private JwtService jwtService;
    @MockBean private CustomUserDetailsService customUserDetailsService;

    @Nested
    @DisplayName("POST /api/auth/register")
    class RegisterEndpoint {

        @Test
        @DisplayName("Valid register request -> 200 with JWT")
        void register_ValidRequest_ReturnsJwt() throws Exception {
            RegisterRequest request = new RegisterRequest(
                    "Ahmed", "Benali", "ahmed@test.com", "password123",
                    "0612345678", "CLIENT", null, null, null
            );

            JwtResponse response = JwtResponse.builder()
                    .accessToken("jwt-token-123")
                    .email("ahmed@test.com")
                    .firstName("Ahmed")
                    .lastName("Benali")
                    .roles(List.of("ROLE_CLIENT"))
                    .build();

            when(authService.register(any(RegisterRequest.class))).thenReturn(response);

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value("jwt-token-123"))
                    .andExpect(jsonPath("$.email").value("ahmed@test.com"))
                    .andExpect(jsonPath("$.firstName").value("Ahmed"))
                    .andExpect(jsonPath("$.roles[0]").value("ROLE_CLIENT"));
        }

        @Test
        @DisplayName("Register with blank email -> 400 validation error")
        void register_BlankEmail_Returns400() throws Exception {
            RegisterRequest request = new RegisterRequest(
                    "Ahmed", "Benali", "", "password123",
                    "0612345678", "CLIENT", null, null, null
            );

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Register with invalid email format -> 400")
        void register_InvalidEmailFormat_Returns400() throws Exception {
            RegisterRequest request = new RegisterRequest(
                    "Ahmed", "Benali", "not-an-email", "password123",
                    "0612345678", "CLIENT", null, null, null
            );

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Register with short password (<8 chars) -> 400")
        void register_ShortPassword_Returns400() throws Exception {
            RegisterRequest request = new RegisterRequest(
                    "Ahmed", "Benali", "ahmed@test.com", "short",
                    "0612345678", "CLIENT", null, null, null
            );

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Register with invalid role -> 400")
        void register_InvalidRole_Returns400() throws Exception {
            RegisterRequest request = new RegisterRequest(
                    "Ahmed", "Benali", "ahmed@test.com", "password123",
                    "0612345678", "INVALID_ROLE", null, null, null
            );

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Register with blank firstName -> 400")
        void register_BlankFirstName_Returns400() throws Exception {
            RegisterRequest request = new RegisterRequest(
                    "", "Benali", "ahmed@test.com", "password123",
                    "0612345678", "CLIENT", null, null, null
            );

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Register with duplicate email -> 409 conflict")
        void register_DuplicateEmail_Returns409() throws Exception {
            RegisterRequest request = new RegisterRequest(
                    "Ahmed", "Benali", "existing@test.com", "password123",
                    "0612345678", "CLIENT", null, null, null
            );

            when(authService.register(any(RegisterRequest.class)))
                    .thenThrow(new EmailAlreadyExistsException("existing@test.com"));

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/login")
    class LoginEndpoint {

        @Test
        @DisplayName("Valid login -> 200 with JWT")
        void login_ValidCredentials_ReturnsJwt() throws Exception {
            LoginRequest request = new LoginRequest("ahmed@test.com", "password123");

            JwtResponse response = JwtResponse.builder()
                    .accessToken("jwt-login-token")
                    .email("ahmed@test.com")
                    .firstName("Ahmed")
                    .lastName("Benali")
                    .roles(List.of("ROLE_CLIENT"))
                    .build();

            when(authService.login(any(LoginRequest.class))).thenReturn(response);

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value("jwt-login-token"))
                    .andExpect(jsonPath("$.email").value("ahmed@test.com"));
        }

        @Test
        @DisplayName("Login with wrong password -> 401")
        void login_WrongPassword_Returns401() throws Exception {
            LoginRequest request = new LoginRequest("ahmed@test.com", "wrongpassword");

            when(authService.login(any(LoginRequest.class)))
                    .thenThrow(new BadCredentialsException("Invalid email or password"));

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Login with blank email -> 400")
        void login_BlankEmail_Returns400() throws Exception {
            LoginRequest request = new LoginRequest("", "password123");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Login with blank password -> 400")
        void login_BlankPassword_Returns400() throws Exception {
            LoginRequest request = new LoginRequest("ahmed@test.com", "");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Login with null body -> 400")
        void login_NullBody_Returns400() throws Exception {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }
}
