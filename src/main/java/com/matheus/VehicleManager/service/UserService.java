package com.matheus.VehicleManager.service;

import com.matheus.VehicleManager.dto.CreateUserDto;
import com.matheus.VehicleManager.dto.LoginUserDto;
import com.matheus.VehicleManager.dto.RecoveryJwtTokenDto;
import com.matheus.VehicleManager.enums.UserRole;
import com.matheus.VehicleManager.model.User;
import com.matheus.VehicleManager.repository.UserRepository;
import com.matheus.VehicleManager.security.authentication.JwtTokenService;
import com.matheus.VehicleManager.security.config.SecurityConfiguration;
import com.matheus.VehicleManager.security.userdetails.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenService jwtTokenService;

    public RecoveryJwtTokenDto authenticateUser(LoginUserDto loginUserDto) {
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                new UsernamePasswordAuthenticationToken(loginUserDto.username(), loginUserDto.password());

        Authentication authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        return new RecoveryJwtTokenDto(jwtTokenService.generateToken(userDetails));
    }
}