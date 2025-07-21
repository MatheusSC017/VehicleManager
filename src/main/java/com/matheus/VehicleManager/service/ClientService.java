package com.matheus.VehicleManager.service;

import com.matheus.VehicleManager.exception.InvalidRequestException;
import com.matheus.VehicleManager.model.Client;
import com.matheus.VehicleManager.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    public Page<Client> findAll(int page, int size) {
        Pageable paging = PageRequest.of(page, size);
        return clientRepository.findAll(paging);
    }

    public List<Client> search(String query) {
        return clientRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrPhoneContaining(query, query, query);
    }

    public Client findByEmail(String email) {
        return clientRepository.findByEmail(email);
    }

    public Client getById(Long clientId) {
        return clientRepository.getReferenceById(clientId);
    }

    public Client create(Client client) {
        return clientRepository.save(client);
    }

    public Client update(Long clientId, Client client) {
        client.setId(clientId);
        return clientRepository.save(client);
    }

    public void delete(Long clientId) {
        Client client = clientRepository.findById(clientId).orElse(null);
        if (client != null) {
            clientRepository.delete(client);
        }
        else {
            Map<String, String> errors = new HashMap<>();
            errors.put("client", "Cliente não encontrado");
            throw new InvalidRequestException(errors);
        }
    }

}
