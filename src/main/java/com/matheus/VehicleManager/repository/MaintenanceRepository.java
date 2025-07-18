package com.matheus.VehicleManager.repository;

import com.matheus.VehicleManager.model.Maintenance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaintenanceRepository  extends JpaRepository<Maintenance, Long> {
    List<Maintenance> findByVehicleIdOrderByIdDesc(Long vehicleId);
}
