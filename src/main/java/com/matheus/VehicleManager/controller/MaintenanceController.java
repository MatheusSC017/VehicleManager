package com.matheus.VehicleManager.controller;

import com.matheus.VehicleManager.dto.MaintenanceRequestDTO;
import com.matheus.VehicleManager.dto.MaintenanceResponseDTO;
import com.matheus.VehicleManager.dto.VehicleMinimalDTO;
import com.matheus.VehicleManager.enums.VehicleStatus;
import com.matheus.VehicleManager.model.Maintenance;
import com.matheus.VehicleManager.model.Vehicle;
import com.matheus.VehicleManager.repository.MaintenanceRepository;
import com.matheus.VehicleManager.repository.VehicleRepository;
import com.matheus.VehicleManager.service.MaintenanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/maintenance")
public class MaintenanceController {

    @Autowired
    private MaintenanceRepository maintenanceRepository;

    @Autowired
    private MaintenanceService maintenanceService;

    @Autowired
    private VehicleRepository vehicleRepository;

    private MaintenanceResponseDTO toDTO(Maintenance maintenance) {
        VehicleMinimalDTO vehicleDTO = new VehicleMinimalDTO(
            maintenance.getVehicle().getId(),
            maintenance.getVehicle().getChassi(),
            maintenance.getVehicle().getBrand(),
            maintenance.getVehicle().getModel()
        );

        return new MaintenanceResponseDTO(
            maintenance.getId(),
            vehicleDTO,
            maintenance.getAdditionalInfo(),
            maintenance.getStartDate(),
            maintenance.getEndDate()
        );
    }

    @GetMapping
    public ResponseEntity<Page<MaintenanceResponseDTO>> getAll(@RequestParam(value = "page", defaultValue = "0") int page,
                                                        @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable paging = PageRequest.of(page, size);
        Page<Maintenance> maintenances = maintenanceRepository.findAll(paging);
        Page<MaintenanceResponseDTO> maintenancesDtoPage = maintenances.map(this::toDTO);
        return ResponseEntity.ok(maintenancesDtoPage);
    }

    @GetMapping("/vehicle/{id}")
    public ResponseEntity<List<MaintenanceResponseDTO>> getAllByVehicle(@PathVariable("id") Long vehicleId) {
        List<Maintenance> maintenances = maintenanceRepository.findByVehicleIdOrderByIdDesc(vehicleId);
        List<MaintenanceResponseDTO> maintenancesDtoPage = maintenances.stream().map(this::toDTO).toList();
        return ResponseEntity.ok(maintenancesDtoPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaintenanceResponseDTO> get(@PathVariable("id") Long saleId) {
        Maintenance maintenance = maintenanceRepository.getReferenceById(saleId);
        return ResponseEntity.ok(toDTO(maintenance));
    }

    @PostMapping
    public ResponseEntity<?> insert(@RequestBody MaintenanceRequestDTO maintenanceRequestDTO) {
        Map<String, Object> response = new HashMap<>();

        Optional<Vehicle> vehicleOpt = vehicleRepository.findById(maintenanceRequestDTO.getVehicleId());
        if (vehicleOpt.isEmpty() || vehicleOpt.get().getVehicleStatus() != VehicleStatus.AVAILABLE) {
            Map<String, String> errors = new HashMap<>();
            if (vehicleOpt.isEmpty()) errors.put("vehicle", "Veículo não encontrado");
            if (vehicleOpt.get().getVehicleStatus() != VehicleStatus.AVAILABLE) errors.put("vehicle", "Veículo não disponível");
            response.put("errors", errors);
            response.put("content", "");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            Maintenance maintenance = maintenanceService.insert(vehicleOpt.get(), maintenanceRequestDTO.getAdditionalInfo());
            return ResponseEntity.status(HttpStatus.OK).body(toDTO(maintenance));
        } catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("sale", e.getMessage());
            response.put("errors", errors);
            response.put("content", "");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long maintenanceId) {
        try {
            boolean response = maintenanceService.delete(maintenanceId);
            if (response) return ResponseEntity.noContent().build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            System.err.println("Failed to delete vehicle: ");
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
