package com.matheus.VehicleManager.controller;

import com.matheus.VehicleManager.dto.*;
import com.matheus.VehicleManager.model.Vehicle;
import com.matheus.VehicleManager.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    @Autowired
    private VehicleService vehicleService;

    private static VehicleResponseDTO toDTO(Vehicle vehicle) {
        return new VehicleResponseDTO(
                vehicle.getId(),
                vehicle.getVehicleType(),
                vehicle.getVehicleStatus(),
                vehicle.getModel(),
                vehicle.getBrand(),
                vehicle.getYear(),
                vehicle.getColor(),
                vehicle.getPlate(),
                vehicle.getChassi(),
                vehicle.getMileage(),
                vehicle.getPrice(),
                vehicle.getVehicleFuel(),
                vehicle.getVehicleChange(),
                vehicle.getDoors(),
                vehicle.getMotor(),
                vehicle.getPower()
        );
    }

    @GetMapping("/images")
    public ResponseEntity<Page<VehicleImageResponseDTO>> getAllWithImages(@RequestParam(value="searchInput", defaultValue="") String search,
                                                                          @RequestParam(value="status", defaultValue="") String status,
                                                                          @RequestParam(value="type", defaultValue="") String type,
                                                                          @RequestParam(value="fuel", defaultValue="") String fuel,
                                                                          @RequestParam(value="priceMin", defaultValue="0") int priceMin,
                                                                          @RequestParam(value="priceMax", defaultValue="0") int priceMax,
                                                                          @RequestParam(value = "page", defaultValue="0") int page,
                                                                          @RequestParam(value = "size", defaultValue="10") int size) {
        Pageable paging = PageRequest.of(page, size);
        Page<VehicleImageResponseDTO> vehiclesPage;
        vehiclesPage = vehicleService.getFilteredVehiclesWithOneImage(
                search,
                status,
                type,
                fuel,
                priceMin,
                priceMax,
                paging
        );
        return ResponseEntity.ok(vehiclesPage);
    }

    @GetMapping
    public ResponseEntity<Page<VehicleResponseDTO>> getAll(@RequestParam(value="searchInput", defaultValue="") String search,
                                                           @RequestParam(value="status", defaultValue="") String status,
                                                           @RequestParam(value="type", defaultValue="") String type,
                                                           @RequestParam(value="fuel", defaultValue="") String fuel,
                                                           @RequestParam(value="priceMin", defaultValue="0") int priceMin,
                                                           @RequestParam(value="priceMax", defaultValue="0") int priceMax,
                                                           @RequestParam(value = "page", defaultValue="0") int page,
                                                           @RequestParam(value = "size", defaultValue="10") int size) {
        Pageable paging = PageRequest.of(page, size);
        Page<Vehicle> vehicles = vehicleService.getFilteredVehicles(
                search,
                status,
                type,
                fuel,
                priceMin,
                priceMax,
                paging
        );
        Page<VehicleResponseDTO> vehiclesPage = vehicles.map(VehicleController::toDTO);
        return ResponseEntity.ok(vehiclesPage);
    }

    @GetMapping("/search")
    public ResponseEntity<List<VehicleResponseDTO>> search(@RequestParam("searchFor") String searchFor) {
        List<Vehicle> vehicles = vehicleService.searchAvailableVehicles(searchFor);
        List<VehicleResponseDTO> vehicleDTOs = vehicles.stream()
                .map(VehicleController::toDTO)
                .toList();
        return ResponseEntity.ok(vehicleDTOs);
    }

    @GetMapping("/chassi/{chassi}")
    public ResponseEntity<?> getByChassi(@PathVariable(value="chassi") String chassi) {
        Vehicle vehicle = vehicleService.findByChassi(chassi);
        return ResponseEntity.ok(toDTO(vehicle));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable(value="id") Long vehicleId) {
        VehicleImagesResponseDTO vehicle = vehicleService.getVehicleWithImagesById(vehicleId);
        return ResponseEntity.ok(vehicle);
    }

    @PostMapping
    public ResponseEntity<?> insert(@Valid @RequestBody VehicleRequestDTO vehicleDto) {
        Vehicle vehicle = vehicleService.create(vehicleDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(vehicle);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable("id") Long vehicleId,
                                   @Valid @RequestBody VehicleRequestDTO vehicleDto) {
        Vehicle vehicle = vehicleService.update(vehicleId, vehicleDto);
        return ResponseEntity.ok(toDTO(vehicle));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long vehicleId) {
        vehicleService.delete(vehicleId);
        return ResponseEntity.noContent().build();
    }

}
