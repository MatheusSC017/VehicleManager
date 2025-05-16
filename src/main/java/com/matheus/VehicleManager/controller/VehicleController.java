package com.matheus.VehicleManager.controller;

import com.matheus.VehicleManager.model.Vehicle;
import com.matheus.VehicleManager.repository.VehicleRepository;
import com.matheus.VehicleManager.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class VehicleController {

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private VehicleRepository vehicleRepository;

    @GetMapping("/veiculos")
    public ModelAndView vehicles() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("vehicles/vehicles");
        modelAndView.addObject("vehiclesList", vehicleRepository.findAll());
        return modelAndView;
    }

    @GetMapping("/veiculos/{id}")
    public ModelAndView vehicle(@PathVariable("id") Long id) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("vehicles/vehicle");
        Vehicle vehicle = vehicleRepository.getReferenceById(id);
        modelAndView.addObject("vehicle", vehicle);
        return modelAndView;
    }

    @GetMapping("/veiculos/cadastrar")
    public ModelAndView getInsertForm(Vehicle vehicle) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("vehicles/vehicle_form");
        modelAndView.addObject("vehicle", new Vehicle());
        return modelAndView;
    }

    @PostMapping("/veiculos/cadastrar")
    public ModelAndView insert(@Valid Vehicle vehicle, BindingResult bindingResult) {
        ModelAndView modelAndView = new ModelAndView();
        if (bindingResult.hasErrors()) {
            modelAndView.setViewName("vehicles/vehicle_form");
            modelAndView.addObject("vehicle", vehicle);
        } else {
            modelAndView.setViewName("redirect:/veiculos");
            vehicleRepository.save(vehicle);
        }
        return modelAndView;
    }

    @GetMapping("/veiculos/{id}/editar")
    public ModelAndView getUpdateForm(@PathVariable("id") Long id) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("vehicles/vehicle_form");
        Vehicle vehicle = vehicleRepository.getReferenceById(id);
        modelAndView.addObject("vehicle", vehicle);
        return modelAndView;
    }

    @PostMapping("/veiculos/{id}/editar")
    public ModelAndView update(@Valid Vehicle vehicle, BindingResult bindingResult) {
        ModelAndView modelAndView = new ModelAndView();
        if (bindingResult.hasErrors()) {
            modelAndView.setViewName("vehicles/vehicle_form");
            modelAndView.addObject("vehicle", vehicle);
        } else {
            modelAndView.setViewName("redirect:/veiculos/" + vehicle.getId());
            vehicleRepository.save(vehicle);
        }
        return modelAndView;
    }

    @GetMapping("/veiculos/{id}/delete")
    public ModelAndView delete() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("vehicles/vehicles");
        return modelAndView;
    }

}
