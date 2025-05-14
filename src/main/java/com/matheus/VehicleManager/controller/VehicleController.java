package com.matheus.VehicleManager.controller;

import com.matheus.VehicleManager.service.VehicleService;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class VehicleController {

    @Autowired
    private VehicleService vehicleService;

    @GetMapping("/veiculos")
    public ModelAndView index() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("vehicles/vehicles");
        return modelAndView;
    }

    @GetMapping("/veiculos/{id}")
    public ModelAndView select() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("vehicles/vehicles");
        return modelAndView;
    }

    @GetMapping("/veiculos/registrar")
    public ModelAndView register() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("vehicles/vehicle_register");
        return modelAndView;
    }

    @GetMapping("/veiculos/editar/{id}")
    public ModelAndView update() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("vehicles/vehicle_register");
        return modelAndView;
    }

}
