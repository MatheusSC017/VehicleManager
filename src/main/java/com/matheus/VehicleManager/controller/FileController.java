package com.matheus.VehicleManager.controller;

import com.matheus.VehicleManager.dto.FileResponseDTO;
import com.matheus.VehicleManager.model.FileStore;
import com.matheus.VehicleManager.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private FileService fileService;

    private static FileResponseDTO toDTO(FileStore file) {
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
                .map(FileController::toDTO)
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
                                    @RequestParam(value="imagesInput") MultipartFile[] images) throws IOException {
        if (images == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        fileService.save(vehicleId, images);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    @PutMapping("/{id}")
    public ResponseEntity<?> update(@RequestParam(value="vehicleId") Long vehicleId,
                                    @RequestParam(value = "imagesInput", required = false) MultipartFile[] images,
                                    @RequestParam(value = "selectedImages", required = false) List<Long> selectedImageIds) throws IOException {
        fileService.update(vehicleId, images, selectedImageIds);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
