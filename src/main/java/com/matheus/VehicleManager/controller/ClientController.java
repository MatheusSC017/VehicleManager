package com.matheus.VehicleManager.controller;

import com.matheus.VehicleManager.dto.ClientResponseDTO;
import com.matheus.VehicleManager.model.Client;
import com.matheus.VehicleManager.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    @Autowired
    private ClientService clientService;

    private ClientResponseDTO toDTO(Client client) {
        return new ClientResponseDTO(
                client.getId(),
                client.getFirstName(),
                client.getLastName(),
                client.getEmail(),
                client.getPhone()
        );
    }

    @GetMapping
    public ResponseEntity<Page<ClientResponseDTO>> getAll(@RequestParam(value = "page", defaultValue = "0") int page,
                                                          @RequestParam(value = "size", defaultValue = "10") int size) {
        Page<Client> clients = clientService.findAll(page, size);
        Page<ClientResponseDTO> clientsDtos = clients.map(this::toDTO);
        return ResponseEntity.ok(clientsDtos);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ClientResponseDTO>> search(@RequestParam("searchFor") String query) {
        List<Client> clients = clientService.search(query);
        List<ClientResponseDTO> clientDTOs = clients.stream()
                .map(this::toDTO)
                .toList();
        return ResponseEntity.ok(clientDTOs);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<ClientResponseDTO> getByEmail(@PathVariable("email") String email) {
        Client client = clientService.findByEmail(email);
        return ResponseEntity.ok(toDTO(client));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientResponseDTO> get(@PathVariable("id") Long clientId) {
        Client client = clientService.getById(clientId);
        return ResponseEntity.ok(toDTO(client));
    }

    @PostMapping
    public ResponseEntity<?> insert(@Valid @RequestBody Client client, BindingResult bindingResult) {
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

        clientService.create(client);

        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(client));
    }


    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable("id") Long clientId, @Valid @RequestBody Client client, BindingResult bindingResult) {
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

        clientService.update(clientId, client);

        return ResponseEntity.ok(toDTO(client));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long clientId) {
        try {
            clientService.delete(clientId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            response.put("errors", errors);
            response.put("content", "");
            return ResponseEntity.badRequest().body(response);
        }
    }

}
