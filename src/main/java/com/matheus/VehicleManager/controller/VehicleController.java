package com.matheus.VehicleManager.controller;

import com.matheus.VehicleManager.service.VehicleService;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class VehicleController {

    @Autowired
    private VehicleService vehicleService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

}
