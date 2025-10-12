package com.matheus.VehicleManager.controller;

import com.matheus.VehicleManager.dto.FileResponseDTO;
import com.matheus.VehicleManager.dto.InsertFileRequestDTO;
import com.matheus.VehicleManager.dto.PresignedRequestDTO;
import com.matheus.VehicleManager.dto.UpdateFileRequestDTO;
import com.matheus.VehicleManager.model.FileStore;
import com.matheus.VehicleManager.service.LocalFileService;
import com.matheus.VehicleManager.service.PresignedFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@Profile("prod")
public class PresignedFileController {

    @Autowired
    private PresignedFileService fileService;

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
                .map(PresignedFileController::toDTO)
                .toList();
        return ResponseEntity.ok(fileStoresDtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable("id") Long fileId) {
        FileStore fileStore = fileService.getById(fileId);
        return ResponseEntity.ok(toDTO(fileStore));
    }


    @PostMapping
    public ResponseEntity<?> insert(@RequestBody InsertFileRequestDTO insertFileRequestDTO) {
        if (insertFileRequestDTO.images() == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        List<String> imagesUploadUrl = fileService.save(insertFileRequestDTO.vehicleId(), insertFileRequestDTO.images());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("uploadUrls", imagesUploadUrl));
    }


    @PutMapping("/{id}")
    public ResponseEntity<?> update(@RequestBody UpdateFileRequestDTO updateFileRequestDTO) {
        List<String> imagesUploadUrl = fileService.update(
                updateFileRequestDTO.vehicleId(),
                updateFileRequestDTO.images(),
                updateFileRequestDTO.selectedImageIds()
        );
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("uploadUrls", imagesUploadUrl));
    }

}
