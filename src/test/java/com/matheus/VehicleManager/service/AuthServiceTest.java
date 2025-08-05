package com.matheus.VehicleManager.service;

import com.matheus.VehicleManager.enums.UserRole;
import com.matheus.VehicleManager.model.User;
import com.matheus.VehicleManager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
public class AuthServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should create a new user")
    void TestCreateUser() {
        String password = "TestPassword";
        String encodedPassword = "EncodedPassword";
        User user = new User();
        user.setUsername("TestUsername");
        user.setPassword(password);
        user.setRole(UserRole.USER);

        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(user);

        User createdUser = authService.createUser(user);

        assertEquals(user, createdUser);
        assertEquals(user.getPassword(), encodedPassword);
        verify(passwordEncoder, times(1)).encode(password);
        verify(userRepository, times(1)).save(any(User.class));
    }

}
