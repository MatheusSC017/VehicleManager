package com.matheus.VehicleManager.controller;

import com.matheus.VehicleManager.dto.LoginUserDto;
import com.matheus.VehicleManager.dto.RecoveryJwtTokenDto;
import com.matheus.VehicleManager.model.User;
import com.matheus.VehicleManager.repository.UserRepository;
import com.matheus.VehicleManager.security.config.SecurityConfiguration;
import com.matheus.VehicleManager.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/usuarios")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private SecurityConfiguration securityConfiguration;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/login")
    public ModelAndView authenticateUser() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("user/login");
        return modelAndView;
    }

    @PostMapping("/login")
    public ResponseEntity<RecoveryJwtTokenDto> authenticateUser(@RequestBody LoginUserDto loginUserDto) {
        RecoveryJwtTokenDto token = userService.authenticateUser(loginUserDto);
        return new ResponseEntity<>(token, HttpStatus.OK);
    }

    @GetMapping("/cadastrar")
    public ModelAndView createUser(User user) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("user/register");
        modelAndView.addObject("user", new User());
        return modelAndView;
    }

    @PostMapping("/cadastrar")
    public ModelAndView createUser(@Valid User user, BindingResult bindingResult) {
        ModelAndView modelAndView = new ModelAndView();
        if (bindingResult.hasErrors()) {
            modelAndView.setViewName("user/register");
            modelAndView.addObject("user", new User());
        } else {
            modelAndView.setViewName("redirect:/usuarios/login");
            user.setPassword(securityConfiguration.passwordEncoder().encode(user.getPassword()));
            userRepository.save(user);
        }
        return modelAndView;
    }

}
