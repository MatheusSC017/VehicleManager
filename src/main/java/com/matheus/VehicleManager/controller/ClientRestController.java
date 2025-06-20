package com.matheus.VehicleManager.controller;

import com.matheus.VehicleManager.model.Client;
import com.matheus.VehicleManager.repository.ClientRepository;
import com.matheus.VehicleManager.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clients")
public class ClientRestController {

    @Autowired
    private ClientService clientService;

    @Autowired
    private ClientRepository clientRepository;

    @GetMapping
    public ResponseEntity<List<Client>> clients() {
        List<Client> clients = clientRepository.findAll();
        return ResponseEntity.ok(clients);
    }

    @PostMapping
    public ResponseEntity<?> register(@Valid Client client, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, Object> response = new HashMap<>();
            response.put("content", client);

            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
            );
            response.put("errors", errors);

            return ResponseEntity.badRequest().body(response);
        }

        clientRepository.save(client);

        return ResponseEntity.status(HttpStatus.CREATED).body(client);
    }


    @PutMapping("/{id}")
    public ResponseEntity<?> update(@Valid Client client, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, Object> response = new HashMap<>();
            response.put("content", client);

            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
            );
            response.put("errors", errors);

            return ResponseEntity.badRequest().body(response);
        }

        clientRepository.save(client);

        return ResponseEntity.ok(client);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long clientId) {
        try {
            clientRepository.deleteById(clientId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            System.err.println("Failed to delete Client: ");
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
