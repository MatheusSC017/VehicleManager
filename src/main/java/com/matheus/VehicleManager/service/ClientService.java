package com.matheus.VehicleManager.service;

import com.matheus.VehicleManager.model.Client;
import com.matheus.VehicleManager.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    public Client findByEmail(String email) {
        return clientRepository.findByEmail(email);
    }

    public boolean isEmailUnique(String email) {
        return email != null && clientRepository.findByEmail(email) == null;
    }

}
