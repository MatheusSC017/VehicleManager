package com.matheus.VehicleManager.controller;

import com.matheus.VehicleManager.dto.AuthRequest;
import com.matheus.VehicleManager.dto.AuthResponse;
import com.matheus.VehicleManager.security.CustomUserDetailsService;
import com.matheus.VehicleManager.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticate(@RequestBody AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        if (authentication.isAuthenticated()) {
            final String token = jwtUtil.generateToken(request.getUsername());
            return ResponseEntity.ok(new AuthResponse(token));
        } else {
            throw new RuntimeException("Invalid credentials");
        }
    }
}
