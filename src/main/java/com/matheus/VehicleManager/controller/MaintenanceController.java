package com.matheus.VehicleManager.controller;

import com.matheus.VehicleManager.dto.MaintenanceRequestDTO;
import com.matheus.VehicleManager.dto.MaintenanceResponseDTO;
import com.matheus.VehicleManager.dto.VehicleMinimalDTO;
import com.matheus.VehicleManager.exception.InvalidRequestException;
import com.matheus.VehicleManager.model.Maintenance;
import com.matheus.VehicleManager.service.MaintenanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/maintenances")
public class MaintenanceController {

    @Autowired
    private MaintenanceService maintenanceService;

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
        Page<Maintenance> maintenances = maintenanceService.findAll(page, size);
        Page<MaintenanceResponseDTO> maintenancesDtoPage = maintenances.map(this::toDTO);
        return ResponseEntity.ok(maintenancesDtoPage);
    }

    @GetMapping("/vehicle/{id}")
    public ResponseEntity<List<MaintenanceResponseDTO>> getAllByVehicle(@PathVariable("id") Long vehicleId) {
        List<Maintenance> maintenances = maintenanceService.findAllByVehicleId(vehicleId);
        List<MaintenanceResponseDTO> maintenancesDtoPage = maintenances.stream().map(this::toDTO).toList();
        return ResponseEntity.ok(maintenancesDtoPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaintenanceResponseDTO> get(@PathVariable("id") Long saleId) {
        Maintenance maintenance = maintenanceService.findById(saleId);
        return ResponseEntity.ok(toDTO(maintenance));
    }

    @PostMapping
    public ResponseEntity<?> insert(@RequestBody MaintenanceRequestDTO maintenanceRequestDTO) {
        try {
            Maintenance maintenance = maintenanceService.create(maintenanceRequestDTO.getVehicleId(), maintenanceRequestDTO.getAdditionalInfo());
            return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(maintenance));
        } catch (InvalidRequestException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("errors", e.getFieldErrors());
            response.put("content", "");
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            response.put("errors", errors);
            response.put("content", "");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long maintenanceId) {
        try {
            maintenanceService.delete(maintenanceId);
            return ResponseEntity.noContent().build();
        } catch (InvalidRequestException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("errors", e.getFieldErrors());
            response.put("content", "");
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            response.put("errors", errors);
            response.put("content", "");
            return ResponseEntity.badRequest().body(response);
        }
    }

}
