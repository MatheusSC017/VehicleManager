package com.matheus.VehicleManager.service;

import com.matheus.VehicleManager.model.Client;
import com.matheus.VehicleManager.repository.ClientRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    @Cacheable(value = "clients_all", key = "#page + '-' + #size")
    public Page<Client> findAll(int page, int size) {
        Pageable paging = PageRequest.of(page, size);
        return clientRepository.findAll(paging);
    }

    @Cacheable(value = "clients_search", key = "#query")
    public List<Client> search(String query) {
        return clientRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrPhoneContaining(query, query, query);
    }

    @Cacheable(value = "clients_by_email", key = "#email")
    public Client findByEmail(String email) {
        return clientRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Client with email " + email + " not found"));
    }

    @Cacheable(value = "clients_by_id", key = "#clientId")
    public Client getById(Long clientId) {
        return clientRepository.findById(clientId)
                .orElseThrow(() -> new EntityNotFoundException("Client with id " + clientId + " not found"));
    }

    @CacheEvict(value = {
            "clients_all", "clients_search", "clients_by_email", "clients_by_id"
    }, allEntries = true)
    public Client create(Client client) {
        return clientRepository.save(client);
    }

    @CacheEvict(value = {
            "clients_all", "clients_search", "clients_by_email", "clients_by_id"
    }, allEntries = true)
    public Client update(Client client) {
        return clientRepository.save(client);
    }

    @CacheEvict(value = {
            "clients_all", "clients_search", "clients_by_email", "clients_by_id"
    }, allEntries = true)
    public void delete(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new EntityNotFoundException("Client with id " + clientId + " not found"));
        clientRepository.delete(client);
    }

}
