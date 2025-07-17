package com.matheus.VehicleManager.controller;

import com.matheus.VehicleManager.dto.*;
import com.matheus.VehicleManager.enums.FileType;
import com.matheus.VehicleManager.enums.VehicleStatus;
import com.matheus.VehicleManager.model.Client;
import com.matheus.VehicleManager.model.FileStore;
import com.matheus.VehicleManager.model.Vehicle;
import com.matheus.VehicleManager.repository.FileRepository;
import com.matheus.VehicleManager.repository.VehicleRepository;
import com.matheus.VehicleManager.service.FileStorageService;
import com.matheus.VehicleManager.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private FileRepository fileRepository;

    private VehicleResponseDTO toDTO(Vehicle vehicle) {
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
    public ResponseEntity<Page<VehicleImageResponseDTO>> getAllWithImages(@RequestParam(value="searchInput") Optional<String> search,
                                                                            @RequestParam("status") Optional<String> status,
                                                                            @RequestParam("type") Optional<String> type,
                                                                            @RequestParam("fuel") Optional<String> fuel,
                                                                            @RequestParam(value="priceMin", defaultValue="0") int priceMin,
                                                                            @RequestParam(value="priceMax", defaultValue="0") int priceMax,
                                                                            @RequestParam(value = "page", defaultValue = "0") int page,
                                                                            @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable paging = PageRequest.of(page, size);
        Page<VehicleImageResponseDTO> vehiclesPage;
        vehiclesPage = vehicleService.getFilteredVehiclesWithOneImage(
                search.orElse(""),
                status.orElse(""),
                type.orElse(""),
                fuel.orElse(""),
                priceMin,
                priceMax,
                paging
        );
        return ResponseEntity.ok(vehiclesPage);
    }

    @GetMapping
    public ResponseEntity<Page<VehicleResponseDTO>> getAll(@RequestParam(value="searchInput") Optional<String> search,
                                                           @RequestParam("status") Optional<String> status,
                                                           @RequestParam("type") Optional<String> type,
                                                           @RequestParam("fuel") Optional<String> fuel,
                                                           @RequestParam(value="priceMin", defaultValue="0") int priceMin,
                                                           @RequestParam(value="priceMax", defaultValue="0") int priceMax,
                                                           @RequestParam(value = "page", defaultValue = "0") int page,
                                                           @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable paging = PageRequest.of(page, size);
        Page<Vehicle> vehicles = vehicleService.getFilteredVehicles(
                search.orElse(""),
                status.orElse(""),
                type.orElse(""),
                fuel.orElse(""),
                priceMin,
                priceMax,
                paging
        );
        Page<VehicleResponseDTO> vehiclesPage = vehicles.map(this::toDTO);
        return ResponseEntity.ok(vehiclesPage);
    }

    @GetMapping("/search")
    public ResponseEntity<List<VehicleResponseDTO>> search(@RequestParam("searchFor") String searchFor) {
        List<Vehicle> vehicles = vehicleRepository.searchAvailableVehicles(searchFor);
        List<VehicleResponseDTO> vehicleDTOs = vehicles.stream()
                .map(this::toDTO)
                .toList();
        return ResponseEntity.ok(vehicleDTOs);
    }

    @GetMapping("/chassi/{chassi}")
    public ResponseEntity<VehicleResponseDTO> getByChassi(@PathVariable(value="chassi") String chassi) {
        Vehicle vehicle = vehicleService.findByChassi(chassi);
        return ResponseEntity.ok(toDTO(vehicle));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleImagesResponseDTO> get(@PathVariable(value="id") Long vehicleId) {
        VehicleImagesResponseDTO vehicle = vehicleService.getVehicleWithImagesById(vehicleId);
        return ResponseEntity.ok(vehicle);
    }

    @PostMapping
    public ResponseEntity<?> insert(@Valid @ModelAttribute VehicleRequestDTO vehicleDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, Object> response = new HashMap<>();
            response.put("content", vehicleDto);

            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
            );
            response.put("errors", errors);

            return ResponseEntity.badRequest().body(response);
        }

        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleType(vehicleDto.getVehicleType());
        vehicle.setModel(vehicleDto.getModel());
        vehicle.setBrand(vehicleDto.getBrand());
        vehicle.setYear(vehicleDto.getYear());
        vehicle.setColor(vehicleDto.getColor());
        vehicle.setPlate(vehicleDto.getPlate());
        vehicle.setChassi(vehicleDto.getChassi());
        vehicle.setMileage(vehicleDto.getMileage());
        vehicle.setPrice(vehicleDto.getPrice());
        vehicle.setVehicleFuel(vehicleDto.getVehicleFuel());
        vehicle.setVehicleChange(vehicleDto.getVehicleChange());
        vehicle.setDoors(vehicleDto.getDoors());
        vehicle.setMotor(vehicleDto.getMotor());
        vehicle.setPower(vehicleDto.getPower());

        vehicleRepository.save(vehicle);

        return ResponseEntity.status(HttpStatus.CREATED).body(vehicle);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                           @Valid @ModelAttribute VehicleRequestDTO vehicleDto,
                                           BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, Object> response = new HashMap<>();
            response.put("content", vehicleDto);

            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            response.put("errors", errors);

            return ResponseEntity.badRequest().body(response);
        }

        Vehicle vehicle = vehicleRepository.getReferenceById(id);
        vehicle.setVehicleType(vehicleDto.getVehicleType());
        vehicle.setModel(vehicleDto.getModel());
        vehicle.setBrand(vehicleDto.getBrand());
        vehicle.setYear(vehicleDto.getYear());
        vehicle.setColor(vehicleDto.getColor());
        vehicle.setPlate(vehicleDto.getPlate());
        vehicle.setChassi(vehicleDto.getChassi());
        vehicle.setMileage(vehicleDto.getMileage());
        vehicle.setPrice(vehicleDto.getPrice());
        vehicle.setVehicleFuel(vehicleDto.getVehicleFuel());
        vehicle.setVehicleChange(vehicleDto.getVehicleChange());
        vehicle.setDoors(vehicleDto.getDoors());
        vehicle.setMotor(vehicleDto.getMotor());
        vehicle.setPower(vehicleDto.getPower());

        vehicleRepository.save(vehicle);

        return ResponseEntity.ok(toDTO(vehicle));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable Long id,
                                             @RequestBody VehicleStatus newStatus) {
        vehicleService.updateStatus(id, newStatus);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long vehicleId) {
        try {
            vehicleRepository.deleteById(vehicleId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            System.err.println("Failed to delete vehicle: ");
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
