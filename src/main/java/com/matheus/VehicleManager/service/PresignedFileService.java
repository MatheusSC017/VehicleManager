package com.matheus.VehicleManager.service;

import com.matheus.VehicleManager.dto.PresignedRequestDTO;
import com.matheus.VehicleManager.enums.FileType;
import com.matheus.VehicleManager.model.FileStore;
import com.matheus.VehicleManager.model.Vehicle;
import com.matheus.VehicleManager.repository.FileRepository;
import com.matheus.VehicleManager.repository.VehicleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
@Profile("prod")
public class PresignedFileService {

    @Value("${aws.region}")
    private String region;

    @Value("${aws.s3-bucket-name}")
    private String bucketName;


    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private VehicleRepository vehicleRepository;


    public List<FileStore> getAll() {
        return fileRepository.findAll();
    }

    public FileStore getById(Long id) {
        return fileRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("File with id " + id + " not found"));
    }


    public List<String> save(Long vehicleId, PresignedRequestDTO[] images) {
        Optional<Vehicle> vehicle = vehicleRepository.findById(vehicleId);
        if (vehicle.isEmpty()) throw new EntityNotFoundException("Vehicle not found");

        List<String> imagesUploadUrl = new ArrayList<>();
        for (PresignedRequestDTO image : images) {
            Map<String, String> imagePresigned = generatePresignedUrl(image);
            imagesUploadUrl.add(imagePresigned.get("uploadUrl"));

            FileStore imageEntity = new FileStore();
            imageEntity.setPath(imagePresigned.get("fileUrl"));
            imageEntity.setType(FileType.IMAGE);
            imageEntity.setVehicle(vehicle.get());

            fileRepository.save(imageEntity);
        }

        return imagesUploadUrl;
    }

    public List<String> update(Long vehicleId, PresignedRequestDTO[] images, List<Long> selectedImageIds) {
        Optional<Vehicle> vehicle = vehicleRepository.findById(vehicleId);
        if (vehicle.isEmpty()) throw new EntityNotFoundException("Vehicle not found");

        List<String> imagesUploadUrl = new ArrayList<>();
        if (images != null) {
            for (PresignedRequestDTO image : images) {
                Map<String, String> imagePresigned = generatePresignedUrl(image);
                imagesUploadUrl.add(imagePresigned.get("uploadUrl"));

                FileStore imageEntity = new FileStore();
                imageEntity.setPath(imagePresigned.get("fileUrl"));
                imageEntity.setType(FileType.IMAGE);
                imageEntity.setVehicle(vehicle.get());

                fileRepository.save(imageEntity);
            }
        }

        if (selectedImageIds != null) {
            for (Long imageId : selectedImageIds) {
                FileStore fileStore = fileRepository.getReferenceById(imageId);
                if (fileStore.getVehicle().getId().equals(vehicleId)) fileRepository.deleteById(imageId);
            }
        }

        return imagesUploadUrl;
    }

    private Map<String, String> generatePresignedUrl(PresignedRequestDTO presignedRequestDTO) {
        String filename = UUID.randomUUID() + "_" + presignedRequestDTO.filename();

        String presignedUrl = fileStorageService.generatePresignedUrl(filename, presignedRequestDTO.contentType());

        String filePath = String.format("https://%s.s3.%s.amazonaws.com/%s",
                bucketName, region, filename);

        return Map.of(
                "uploadUrl", presignedUrl,
                "fileUrl", filePath
        );
    }

}
