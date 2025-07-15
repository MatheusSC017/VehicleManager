package com.matheus.VehicleManager.repository;

import com.matheus.VehicleManager.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClientRepository extends JpaRepository<Client, Long> {

    Client findByEmail(String email);

    List<Client> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrPhoneContaining(String firstName, String lastName, String phone);

}
