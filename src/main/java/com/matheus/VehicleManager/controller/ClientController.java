package com.matheus.VehicleManager.controller;

import com.matheus.VehicleManager.model.Client;
import com.matheus.VehicleManager.repository.ClientRepository;
import com.matheus.VehicleManager.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/clientes")
public class ClientController {

    @Autowired
    private ClientService clientService;

    @Autowired
    private ClientRepository clientRepository;

    @GetMapping
    public ModelAndView clients() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("clients/clients");
        modelAndView.addObject("clients", clientRepository.findAll());
        return modelAndView;
    }

    @GetMapping("/cadastrar")
    public ModelAndView register(Client client) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("clients/client_form");
        modelAndView.addObject("client", new Client());
        return modelAndView;
    }

    @PostMapping("/cadastrar")
    public ModelAndView register(@Valid Client client, BindingResult bindingResult) {
        ModelAndView modelAndView = new ModelAndView();
        if (bindingResult.hasErrors()) {
            modelAndView.setViewName("clients/client_form");
            modelAndView.addObject("client", client);
        } else {
            modelAndView.setViewName("redirect:/clientes");
            clientRepository.save(client);
        }
        return modelAndView;
    }

}
