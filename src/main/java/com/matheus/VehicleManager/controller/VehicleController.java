package com.matheus.VehicleManager.controller;

import com.matheus.VehicleManager.dto.VehicleImageResponseDTO;
import com.matheus.VehicleManager.dto.VehicleImagesResponseDTO;
import com.matheus.VehicleManager.enums.FileType;
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

    @GetMapping
    public ResponseEntity<Page<VehicleImageResponseDTO>> get_vehicles(@RequestParam(value="searchInput") Optional<String> search,
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

    @GetMapping("/{id}")
    public ResponseEntity<VehicleImagesResponseDTO> get_vehicle(@PathVariable(value="id") Long vehicleId) {
        VehicleImagesResponseDTO vehicle = vehicleService.getVehicleWithImagesById(vehicleId);
        return ResponseEntity.ok(vehicle);
    }

    @PostMapping
    public ResponseEntity<?> insertVehicle(@Valid @ModelAttribute Vehicle vehicle,
                                           BindingResult bindingResult,
                                           @RequestParam(value="imagesInput", required = false) MultipartFile[] images) {
        if (bindingResult.hasErrors()) {
            Map<String, Object> response = new HashMap<>();
            response.put("content", vehicle);

            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
            );
            response.put("errors", errors);

            return ResponseEntity.badRequest().body(response);
        }


        vehicleRepository.save(vehicle);

        if (images != null) {
            for (MultipartFile image : images) {
                if (image.isEmpty()) continue;

                try {
                    String path = fileStorageService.storeFile(image);

                    FileStore imageEntity = new FileStore();
                    imageEntity.setPath(path);
                    imageEntity.setType(FileType.IMAGE);
                    imageEntity.setVehicle(vehicle);

                    fileRepository.save(imageEntity);
                } catch (IOException e) {
                    System.err.println("Failed to store image: " + image.getOriginalFilename());
                    e.printStackTrace();
                }
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(vehicle);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateVehicle(@PathVariable Long id,
                                           @Valid @ModelAttribute Vehicle vehicle,
                                           BindingResult bindingResult,
                                           @RequestParam(value = "imagesInput", required = false) MultipartFile[] images,
                                           @RequestParam(value = "selectedImages", required = false) List<Long> selectedImageIds) {
        if (bindingResult.hasErrors()) {
            Map<String, Object> response = new HashMap<>();
            response.put("content", vehicle);

            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            response.put("errors", errors);

            return ResponseEntity.badRequest().body(response);
        }

        vehicle.setId(id);
        vehicleRepository.save(vehicle);

        if (images != null) {
            for (MultipartFile image : images) {
                if (image.isEmpty()) continue;

                try {
                    String path = fileStorageService.storeFile(image);

                    FileStore imageEntity = new FileStore();
                    imageEntity.setPath(path);
                    imageEntity.setType(FileType.IMAGE);
                    imageEntity.setVehicle(vehicle);

                    fileRepository.save(imageEntity);
                } catch (IOException e) {
                    System.err.println("Failed to store image: " + image.getOriginalFilename());
                    e.printStackTrace();
                }
            }
        }

        if (selectedImageIds != null) {
            for (Long imageId : selectedImageIds) {
                try {
                    fileRepository.deleteById(imageId);
                } catch (Exception e) {
                    System.err.println("Failed to delete image with ID: " + imageId);
                    e.printStackTrace();
                }
            }
        }

        return ResponseEntity.ok(vehicle);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVehicle(@PathVariable("id") Long vehicleId) {
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
