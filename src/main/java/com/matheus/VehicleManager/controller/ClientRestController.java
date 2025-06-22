package com.matheus.VehicleManager.controller;

import com.matheus.VehicleManager.dto.ClientDTO;
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

    private ClientDTO toDTO(Client client) {
        return new ClientDTO(
                client.getId(),
                client.getFirstName(),
                client.getLastName(),
                client.getEmail(),
                client.getPhone()
        );
    }

    @GetMapping
    public ResponseEntity<List<ClientDTO>> clients() {
        List<Client> clients = clientRepository.findAll();
        List<ClientDTO> clientsDtos = clients.stream()
                .map(this::toDTO)
                .toList();
        return ResponseEntity.ok(clientsDtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientDTO> client(@PathVariable("id") Long clientId) {
        Client client = clientRepository.getReferenceById(clientId);
        return ResponseEntity.ok(toDTO(client));
    }

    @PostMapping
    public ResponseEntity<?> register(@Valid Client client, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, Object> response = new HashMap<>();
            response.put("content", toDTO(client));

            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
            );
            response.put("errors", errors);

            return ResponseEntity.badRequest().body(response);
        }

        clientRepository.save(client);

        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(client));
    }


    @PutMapping("/{id}")
    public ResponseEntity<?> update(@Valid Client client, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, Object> response = new HashMap<>();
            response.put("content", toDTO(client));

            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
            );
            response.put("errors", errors);

            return ResponseEntity.badRequest().body(response);
        }

        clientRepository.save(client);

        return ResponseEntity.ok(toDTO(client));
    }

    @DeleteMapping("/{id}")
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
