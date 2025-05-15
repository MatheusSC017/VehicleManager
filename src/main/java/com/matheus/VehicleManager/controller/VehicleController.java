package com.matheus.VehicleManager.controller;

import com.matheus.VehicleManager.model.Vehicle;
import com.matheus.VehicleManager.repository.VehicleRepository;
import com.matheus.VehicleManager.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class VehicleController {

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private VehicleRepository vehicleRepository;

    @GetMapping("/veiculos")
    public ModelAndView index() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("vehicles/vehicles");
        System.out.println(vehicleRepository.findAll());
        modelAndView.addObject("vehiclesList", vehicleRepository.findAll());
        return modelAndView;
    }

    @GetMapping("/veiculos/{id}")
    public ModelAndView select() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("vehicles/vehicles");
        return modelAndView;
    }

    @GetMapping("/veiculos/cadastrar")
    public ModelAndView getInsertVehicleForm(Vehicle vehicle) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("vehicles/vehicle_register");
        modelAndView.addObject("vehicle", new Vehicle());
        return modelAndView;
    }

    @PostMapping("/veiculos/cadastrar")
    public ModelAndView insertVehicle(@Valid Vehicle vehicle, BindingResult bindingResult) {
        ModelAndView modelAndView = new ModelAndView();
        if (bindingResult.hasErrors()) {
            System.out.println(bindingResult.getAllErrors());
            modelAndView.setViewName("vehicles/vehicle_register");
            modelAndView.addObject("vehicle");
        } else {
            System.out.println("HEREEEEEEEEEEEEEEEEEEEEEEE22");
            modelAndView.setViewName("redirect:/veiculos");
            vehicleRepository.save(vehicle);
        }
        return modelAndView;
    }

    @GetMapping("/veiculos/editar/{id}")
    public ModelAndView update() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("vehicles/vehicle_register");
        return modelAndView;
    }

}
