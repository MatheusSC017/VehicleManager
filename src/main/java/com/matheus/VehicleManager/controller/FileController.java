package com.matheus.VehicleManager.controller;

import com.matheus.VehicleManager.dto.FileResponseDTO;
import com.matheus.VehicleManager.enums.FileType;
import com.matheus.VehicleManager.model.FileStore;
import com.matheus.VehicleManager.model.Vehicle;
import com.matheus.VehicleManager.repository.FileRepository;
import com.matheus.VehicleManager.repository.VehicleRepository;
import com.matheus.VehicleManager.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    private FileResponseDTO toDTO(FileStore file) {
        return new FileResponseDTO(
            file.getId(),
            file.getPath(),
            file.getType(),
            file.getVehicle().getId()
        );

    }

    @GetMapping
    public ResponseEntity<List<FileResponseDTO>> getAll() {
        List<FileStore> fileStores = fileRepository.findAll();
        List<FileResponseDTO> fileStoresDtos = fileStores.stream()
                .map(this::toDTO)
                .toList();
        return ResponseEntity.ok(fileStoresDtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FileResponseDTO> get(@PathVariable("id") Long fileId) {
        FileStore fileStore = fileRepository.getReferenceById(fileId);
        return ResponseEntity.ok(toDTO(fileStore));
    }

    @PostMapping
    public ResponseEntity<?> insert(@RequestParam(value="vehicleId") Long vehicleId,
                                    @RequestParam(value="imagesInput") MultipartFile[] images) {
        if (images == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        Optional<Vehicle> vehicle = vehicleRepository.findById(vehicleId);
        if (vehicle.isEmpty()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        for (MultipartFile image : images) {
            if (image.isEmpty()) continue;

            try {
                String path = fileStorageService.storeFile(image);

                FileStore imageEntity = new FileStore();
                imageEntity.setPath(path);
                imageEntity.setType(FileType.IMAGE);
                imageEntity.setVehicle(vehicle.get());

                fileRepository.save(imageEntity);
            } catch (IOException e) {
                System.err.println("Failed to store image: " + image.getOriginalFilename());
                e.printStackTrace();
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    @PutMapping("/{id}")
    public ResponseEntity<?> update(@RequestParam(value="vehicleId") Long vehicleId,
                                    @RequestParam(value = "imagesInput", required = false) MultipartFile[] images,
                                    @RequestParam(value = "selectedImages", required = false) List<Long> selectedImageIds) {
        Optional<Vehicle> vehicle = vehicleRepository.findById(vehicleId);
        if (vehicle.isEmpty()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        if (images != null) {
            for (MultipartFile image : images) {
                if (image.isEmpty()) continue;

                try {
                    String path = fileStorageService.storeFile(image);

                    FileStore imageEntity = new FileStore();
                    imageEntity.setPath(path);
                    imageEntity.setType(FileType.IMAGE);
                    imageEntity.setVehicle(vehicle.get());

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
                    FileStore fileStore = fileRepository.getReferenceById(imageId);
                    if (fileStore.getVehicle().getId().equals(vehicleId)) fileRepository.deleteById(imageId);
                } catch (Exception e) {
                    System.err.println("Failed to delete image with ID: " + imageId);
                    e.printStackTrace();
                }
            }
        }

        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
