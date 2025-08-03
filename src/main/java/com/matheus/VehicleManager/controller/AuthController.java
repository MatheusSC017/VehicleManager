package com.matheus.VehicleManager.controller;

import com.matheus.VehicleManager.dto.AuthRequestDTO;
import com.matheus.VehicleManager.dto.AuthResponseDTO;
import com.matheus.VehicleManager.dto.UsernameRequestDTO;
import com.matheus.VehicleManager.security.CustomUserDetailsService;
import com.matheus.VehicleManager.security.JwtUtil;
import com.matheus.VehicleManager.model.User;
import com.matheus.VehicleManager.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticate(@RequestBody AuthRequestDTO request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        if (authentication.isAuthenticated()) {
            final String token = jwtUtil.generateToken(request.getUsername());
            return ResponseEntity.ok(new AuthResponseDTO(token));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid credentials");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> createUser(@Valid @RequestBody User user, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, Object> response = new HashMap<>();
            user.setPassword("");
            response.put("content", user);

            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            response.put("errors", errors);

            return ResponseEntity.badRequest().body(response);
        }

        authService.createUser(user);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String authHeader, @RequestBody UsernameRequestDTO request) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Authorization header");
        }

        String token = authHeader.substring(7);

        try {
            String tokenUsername = jwtUtil.extractUsername(token);
            if (jwtUtil.validateToken(token) && tokenUsername.equals(request.getUsername())) {
                String newToken = jwtUtil.generateToken(tokenUsername);
                return ResponseEntity.ok(new AuthResponseDTO(newToken));
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Token validation failed");
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token is invalid or expired");
        }
    }
}
