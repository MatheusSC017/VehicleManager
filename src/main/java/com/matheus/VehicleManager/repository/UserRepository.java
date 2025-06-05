package com.matheus.VehicleManager.repository;

import com.matheus.VehicleManager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}
