package com.matheus.VehicleManager.service;

import com.matheus.VehicleManager.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    public boolean isEmailUnique(String email) {
        return email != null && clientRepository.findByEmail(email) == null;
    }

}
