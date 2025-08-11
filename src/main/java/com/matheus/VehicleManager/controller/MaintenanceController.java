package com.matheus.VehicleManager.controller;

import com.matheus.VehicleManager.dto.MaintenanceRequestDTO;
import com.matheus.VehicleManager.dto.MaintenanceResponseDTO;
import com.matheus.VehicleManager.dto.VehicleMinimalDTO;
import com.matheus.VehicleManager.model.Maintenance;
import com.matheus.VehicleManager.service.MaintenanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/maintenances")
public class MaintenanceController {

    @Autowired
    private MaintenanceService maintenanceService;

    private static MaintenanceResponseDTO toDTO(Maintenance maintenance) {
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
        Page<Maintenance> maintenances = maintenanceService.findAll(page, size);
        Page<MaintenanceResponseDTO> maintenancesDtoPage = maintenances.map(MaintenanceController::toDTO);
        return ResponseEntity.ok(maintenancesDtoPage);
    }

    @GetMapping("/vehicle/{id}")
    public ResponseEntity<List<MaintenanceResponseDTO>> getAllByVehicle(@PathVariable("id") Long vehicleId) {
        List<Maintenance> maintenances = maintenanceService.findAllByVehicleId(vehicleId);
        List<MaintenanceResponseDTO> maintenancesDtoPage = maintenances.stream().map(MaintenanceController::toDTO).toList();
        return ResponseEntity.ok(maintenancesDtoPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable("id") Long saleId) {
        Maintenance maintenance = maintenanceService.findById(saleId);
        return ResponseEntity.ok(toDTO(maintenance));
    }

    @PostMapping
    public ResponseEntity<?> insert(@RequestBody MaintenanceRequestDTO maintenanceRequestDTO) {
        Maintenance maintenance = maintenanceService.create(maintenanceRequestDTO.getVehicleId(), maintenanceRequestDTO.getAdditionalInfo());
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(maintenance));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long maintenanceId) {
        maintenanceService.delete(maintenanceId);
        return ResponseEntity.noContent().build();
    }

}
