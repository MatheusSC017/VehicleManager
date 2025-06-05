package com.matheus.VehicleManager.controller;

import com.matheus.VehicleManager.model.User;
import com.matheus.VehicleManager.security.SecurityConfiguration;
import com.matheus.VehicleManager.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private SecurityConfiguration securityConfiguration;

    @GetMapping("/login")
    public ModelAndView authenticateUser() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("users/login");
        return modelAndView;
    }

    @GetMapping("/usuarios/cadastrar")
    public ModelAndView createUser(User user) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("users/register");
        modelAndView.addObject("user", new User());
        return modelAndView;
    }

    @PostMapping("/usuarios/cadastrar")
    public ModelAndView createUser(@Valid User user, BindingResult bindingResult) {
        ModelAndView modelAndView = new ModelAndView();
        if (bindingResult.hasErrors()) {
            modelAndView.setViewName("users/register");
            modelAndView.addObject("user", user);
        } else {
            user.setPassword(securityConfiguration.passwordEncoder().encode(user.getPassword()));
            userService.saveUser(user);
            modelAndView.setViewName("redirect:/login");
        }
        return modelAndView;
    }

}
