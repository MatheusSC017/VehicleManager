package com.matheus.VehicleManager.service;

import com.matheus.VehicleManager.enums.VehicleStatus;
import com.matheus.VehicleManager.model.Maintenance;
import com.matheus.VehicleManager.model.Vehicle;
import com.matheus.VehicleManager.repository.MaintenanceRepository;
import com.matheus.VehicleManager.repository.VehicleRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MaintenanceService {

    @Autowired
    private MaintenanceRepository maintenanceRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Transactional
    public Maintenance insert(Vehicle vehicle) {
        vehicle.setVehicleStatus(VehicleStatus.MAINTENANCE);
        vehicleRepository.save(vehicle);

        Maintenance maintenance = new Maintenance();
        maintenance.setVehicle(vehicle);
        maintenanceRepository.save(maintenance);
        return maintenance;
    }

    @Transactional
    public boolean delete(Long maintenanceId) {
        Optional<Maintenance> maintenance = maintenanceRepository.findById(maintenanceId);
        if (maintenance.isEmpty()) return false;

        Vehicle vehicle = maintenance.get().getVehicle();
        vehicle.setVehicleStatus(VehicleStatus.AVAILABLE);
        vehicleRepository.save(vehicle);

        maintenanceRepository.delete(maintenance.get());
        return true;
    }

}
