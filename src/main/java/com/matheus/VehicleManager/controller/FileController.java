package com.matheus.VehicleManager.controller;

import com.matheus.VehicleManager.dto.FileResponseDTO;
import com.matheus.VehicleManager.model.FileStore;
import com.matheus.VehicleManager.security.JwtAuthenticationFilter;
import com.matheus.VehicleManager.security.JwtUtil;
import com.matheus.VehicleManager.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private FileService fileService;

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
        List<FileStore> fileStores = fileService.getAll();
        List<FileResponseDTO> fileStoresDtos = fileStores.stream()
                .map(this::toDTO)
                .toList();
        return ResponseEntity.ok(fileStoresDtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable("id") Long fileId) {
        FileStore fileStore = fileService.getById(fileId);
        return ResponseEntity.ok(toDTO(fileStore));
    }

    @PostMapping
    public ResponseEntity<?> insert(@RequestParam(value="vehicleId") Long vehicleId,
                                    @RequestParam(value="imagesInput") MultipartFile[] images) {
        if (images == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        try {
            fileService.save(vehicleId, images);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (IOException e) {
            System.err.println("Failed to store images: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


    @PutMapping("/{id}")
    public ResponseEntity<?> update(@RequestParam(value="vehicleId") Long vehicleId,
                                    @RequestParam(value = "imagesInput", required = false) MultipartFile[] images,
                                    @RequestParam(value = "selectedImages", required = false) List<Long> selectedImageIds) {
        try {
            fileService.update(vehicleId, images, selectedImageIds);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (IOException e) {
            System.err.println("Failed to update files: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

}
