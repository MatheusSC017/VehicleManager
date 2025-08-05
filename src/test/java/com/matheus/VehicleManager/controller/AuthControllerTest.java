package com.matheus.VehicleManager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matheus.VehicleManager.dto.AuthRequestDTO;
import com.matheus.VehicleManager.dto.UsernameRequestDTO;
import com.matheus.VehicleManager.enums.UserRole;

import com.matheus.VehicleManager.model.User;
import com.matheus.VehicleManager.repository.UserRepository;
import com.matheus.VehicleManager.security.CustomUserDetailsService;
import com.matheus.VehicleManager.security.JwtUtil;
import com.matheus.VehicleManager.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private AuthService authService;

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Should return the token for successful login")
    void testSuccessfulLogin() throws Exception {
        AuthRequestDTO authRequestDTO = new AuthRequestDTO();
        authRequestDTO.setUsername("UsernameTest");
        authRequestDTO.setPassword("PasswordTest");

        Authentication authMock = mock(Authentication.class);
        when(authMock.isAuthenticated()).thenReturn(true);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authMock);
        when(jwtUtil.generateToken("UsernameTest")).thenReturn("TestToken");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(authRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("TestToken"));
    }

    @Test
    @DisplayName("Should return a error for invalid credentials login")
    void testLoginInvalidCredentials() throws Exception {
        AuthRequestDTO authRequestDTO = new AuthRequestDTO();
        authRequestDTO.setUsername("UsernameTest");
        authRequestDTO.setPassword("PasswordTest");

        Authentication authMock = mock(Authentication.class);
        when(authMock.isAuthenticated()).thenReturn(false);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authMock);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(authRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Invalid credentials"));
    }

    @Test
    @DisplayName("Should return the new token for successful refresh")
    void testSuccessfulRefresh() throws Exception {
        UsernameRequestDTO usernameRequestDTO = new UsernameRequestDTO();
        usernameRequestDTO.setUsername("UsernameTest");

        when(jwtUtil.extractUsername("TestToken")).thenReturn("UsernameTest");
        when(jwtUtil.validateToken("TestToken")).thenReturn(true);
        when(jwtUtil.generateToken("UsernameTest")).thenReturn("TestNewToken");

        mockMvc.perform(post("/auth/refresh")
                        .header("Authorization", "Bearer TestToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(usernameRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("TestNewToken"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"TestToken", ""})
    @DisplayName("Should return a error if authorization not sent or not valid")
    void testRefreshMissingAuthorization(String authorization) throws Exception {
        UsernameRequestDTO usernameRequestDTO = new UsernameRequestDTO();
        usernameRequestDTO.setUsername("UsernameTest");

        mockMvc.perform(post("/auth/refresh")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(usernameRequestDTO)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$").value("Invalid Authorization header"));
    }

    @Test
    @DisplayName("Should return a error if authorization not valid")
    void testRefreshInvalid() throws Exception {
        UsernameRequestDTO usernameRequestDTO = new UsernameRequestDTO();
        usernameRequestDTO.setUsername("UsernameTest");

        when(jwtUtil.extractUsername("TestToken")).thenReturn("UsernameTest");
        when(jwtUtil.validateToken("TestToken")).thenReturn(false);

        mockMvc.perform(post("/auth/refresh")
                        .header("Authorization", "Bearer TestToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(usernameRequestDTO)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$").value("Token validation failed"));
    }

    @Test
    @DisplayName("Should return a error if token user different")
    void testRefreshDifferentUser() throws Exception {
        UsernameRequestDTO usernameRequestDTO = new UsernameRequestDTO();
        usernameRequestDTO.setUsername("UsernameTest");

        when(jwtUtil.extractUsername("TestToken")).thenReturn("DifferentUsernameTest");
        when(jwtUtil.validateToken("TestToken")).thenReturn(true);

        mockMvc.perform(post("/auth/refresh")
                        .header("Authorization", "Bearer TestToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(usernameRequestDTO)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$").value("Token validation failed"));
    }

    @Test
    @DisplayName("Should create a new user")
    void testCreateSuccess() throws Exception {
        User user = new User();
        user.setUsername("UsernameTest");
        user.setPassword("PasswordTest");
        user.setRole(UserRole.USER);

        when(authService.createUser(any(User.class))).thenReturn(user);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(user)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Should throw a validation error when try create a new user without required data")
    void testCreateValidationError() throws Exception {
        User user = new User();

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.content").exists());
    }
}
