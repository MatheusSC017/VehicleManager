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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.util.List;

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

    @PostMapping("/veiculos/pesquisa")
    public ModelAndView searcVehicles(@RequestParam("searchInput") String search) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("vehicles/vehicles");
        List<Vehicle> filteredVehicles = vehicleRepository.findByBrandAndModelIgnoreCase(search);
        modelAndView.addObject("vehiclesList", filteredVehicles);
        return modelAndView;
    }

    @GetMapping("/veiculos/filtro")
    public ModelAndView filterVehicles(@RequestParam("status") String status, @RequestParam("type") String type,
                                       @RequestParam("fuel") String fuel, @RequestParam("priceMin") int priceMin,
                                       @RequestParam("priceMax") int priceMax) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("vehicles/vehicles");
        List<Vehicle> vehicles = vehicleRepository.findAll();
        vehicles = vehicles.stream()
                .filter(v -> status.isEmpty() || v.getVehicleStatus().name().equalsIgnoreCase(status))
                .filter(v -> type.isEmpty() || v.getVehicleType().name().equalsIgnoreCase(type))
                .filter(v -> fuel.isEmpty() || v.getVehicleFuel().name().equalsIgnoreCase(fuel))
                .filter(v -> priceMin == 0 || v.getPrice().compareTo(BigDecimal.valueOf(priceMin)) >= 0)
                .filter(v -> priceMax == 0 || v.getPrice().compareTo(BigDecimal.valueOf(priceMax)) <= 0)
                .toList();
        modelAndView.addObject("statusFilter", status);
        modelAndView.addObject("typeFilter", type);
        modelAndView.addObject("fuelFilter", fuel);
        modelAndView.addObject("priceMinFilter", priceMin);
        modelAndView.addObject("priceMaxFilter", priceMax);
        modelAndView.addObject("vehiclesList", vehicles);
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

    @GetMapping("/veiculos/{id}/deletar")
    public ModelAndView delete(@PathVariable("id") Long id) {
        Vehicle vehicle = vehicleRepository.getReferenceById(id);
        if (vehicle != null) {
            vehicleRepository.delete(vehicle);
        }

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("vehicles/vehicles");
        modelAndView.addObject("vehiclesList", vehicleRepository.findAll());
        return modelAndView;
    }

}
