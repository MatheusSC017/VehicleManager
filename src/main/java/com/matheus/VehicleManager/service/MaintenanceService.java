package com.matheus.VehicleManager.service;

import com.matheus.VehicleManager.enums.VehicleStatus;
import com.matheus.VehicleManager.model.Maintenance;
import com.matheus.VehicleManager.model.Vehicle;
import com.matheus.VehicleManager.repository.MaintenanceRepository;
import com.matheus.VehicleManager.repository.VehicleRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class MaintenanceService {

    @Autowired
    private MaintenanceRepository maintenanceRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Transactional
    public Maintenance insert(Vehicle vehicle, String additionalInfo) {
        vehicle.setVehicleStatus(VehicleStatus.MAINTENANCE);
        vehicleRepository.save(vehicle);

        Maintenance maintenance = new Maintenance();
        maintenance.setVehicle(vehicle);
        maintenance.setAdditionalInfo(additionalInfo);
        maintenanceRepository.save(maintenance);
        return maintenance;
    }

    @Transactional
    public boolean delete(Long maintenanceId) {
        Optional<Maintenance> maintenanceOpt = maintenanceRepository.findById(maintenanceId);
        if (maintenanceOpt.isEmpty()) return false;
        Maintenance maintenance = maintenanceOpt.get();

        Vehicle vehicle = maintenance.getVehicle();
        vehicle.setVehicleStatus(VehicleStatus.AVAILABLE);
        vehicleRepository.save(vehicle);

        maintenance.setEndDate(LocalDate.now());
        maintenanceRepository.save(maintenance);
        return true;
    }

}
