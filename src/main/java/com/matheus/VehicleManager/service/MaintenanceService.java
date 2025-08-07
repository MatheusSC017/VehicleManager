package com.matheus.VehicleManager.service;

import com.matheus.VehicleManager.enums.VehicleStatus;
import com.matheus.VehicleManager.exception.InvalidRequestException;
import com.matheus.VehicleManager.model.Maintenance;
import com.matheus.VehicleManager.model.Vehicle;
import com.matheus.VehicleManager.repository.MaintenanceRepository;
import com.matheus.VehicleManager.repository.VehicleRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MaintenanceService {

    @Autowired
    private MaintenanceRepository maintenanceRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    public Page<Maintenance> findAll(int page, int size) {
        Pageable paging = PageRequest.of(page, size);
        return maintenanceRepository.findAll(paging);
    }

    public List<Maintenance> findAllByVehicleId(Long vehicleId) {
        return maintenanceRepository.findByVehicleIdOrderByIdDesc(vehicleId);
    }

    public Maintenance findById(Long maintenanceId) {
        return maintenanceRepository.findById(maintenanceId)
                .orElseThrow(() -> new EntityNotFoundException("Maintenance with id " + maintenanceId + " not found"));
    }

    @Transactional
    public Maintenance create(Long vehicleId, String additionalInfo) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId).orElse(null);

        Map<String, String> errors = new HashMap<>();
        if (vehicle == null) errors.put("vehicle", "Veículo não encontrado");
        else if (vehicle.getVehicleStatus() != VehicleStatus.AVAILABLE) errors.put("vehicle", "Veículo não disponível");

        if (!errors.isEmpty()) throw new InvalidRequestException(errors);

        vehicle.setVehicleStatus(VehicleStatus.MAINTENANCE);
        vehicleRepository.save(vehicle);

        Maintenance maintenance = new Maintenance();
        maintenance.setVehicle(vehicle);
        maintenance.setAdditionalInfo(additionalInfo);
        return maintenanceRepository.save(maintenance);
    }

    @Transactional
    public void delete(Long maintenanceId) {
        Maintenance maintenance = maintenanceRepository.getReferenceById(maintenanceId);
        Vehicle vehicle = maintenance.getVehicle();
        vehicle.setVehicleStatus(VehicleStatus.AVAILABLE);
        vehicleRepository.save(vehicle);

        maintenance.setEndDate(LocalDate.now());
        maintenanceRepository.save(maintenance);
    }

}
