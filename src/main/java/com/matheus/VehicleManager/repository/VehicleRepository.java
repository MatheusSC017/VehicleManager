package com.matheus.VehicleManager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.matheus.VehicleManager.model.Vehicle;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
}
