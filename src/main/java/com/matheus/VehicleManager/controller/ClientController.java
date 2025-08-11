package com.matheus.VehicleManager.controller;

import com.matheus.VehicleManager.dto.ClientResponseDTO;
import com.matheus.VehicleManager.model.Client;
import com.matheus.VehicleManager.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    @Autowired
    private ClientService clientService;

    private static ClientResponseDTO toDTO(Client client) {
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
        Page<ClientResponseDTO> clientsDtos = clients.map(ClientController::toDTO);
        return ResponseEntity.ok(clientsDtos);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ClientResponseDTO>> search(@RequestParam("searchFor") String query) {
        List<Client> clients = clientService.search(query);
        List<ClientResponseDTO> clientDTOs = clients.stream()
                .map(ClientController::toDTO)
                .toList();
        return ResponseEntity.ok(clientDTOs);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<?> getByEmail(@PathVariable("email") String email) {
        Client client = clientService.findByEmail(email);
        return ResponseEntity.ok(toDTO(client));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable("id") Long clientId) {
        Client client = clientService.getById(clientId);
        return ResponseEntity.ok(toDTO(client));
    }

    @PostMapping
    public ResponseEntity<?> insert(@Valid @RequestBody Client client) {
        clientService.create(client);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(client));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable("id") Long clientId, @Valid @RequestBody Client client) {
        clientService.update(clientId, client);
        return ResponseEntity.ok(toDTO(client));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long clientId) {
        clientService.delete(clientId);
        return ResponseEntity.noContent().build();
    }

}
