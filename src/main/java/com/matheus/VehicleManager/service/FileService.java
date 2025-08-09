package com.matheus.VehicleManager.service;

import com.matheus.VehicleManager.enums.FileType;
import com.matheus.VehicleManager.model.FileStore;
import com.matheus.VehicleManager.model.Vehicle;
import com.matheus.VehicleManager.repository.FileRepository;
import com.matheus.VehicleManager.repository.VehicleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class FileService {

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

    public void save(Long vehicleId, MultipartFile[] images) throws IOException {
        Optional<Vehicle> vehicle = vehicleRepository.findById(vehicleId);
        if (vehicle.isEmpty()) throw new IOException("Vehicle not found");

        for (MultipartFile image : images) {
            if (image.isEmpty()) continue;

            String path = fileStorageService.storeFile(image);

            FileStore imageEntity = new FileStore();
            imageEntity.setPath(path);
            imageEntity.setType(FileType.IMAGE);
            imageEntity.setVehicle(vehicle.get());

            fileRepository.save(imageEntity);
        }
    }

    public void update(Long vehicleId, MultipartFile[] images, List<Long> selectedImageIds) throws IOException {
        Optional<Vehicle> vehicle = vehicleRepository.findById(vehicleId);
        if (vehicle.isEmpty()) throw new IOException("Vehicle not found");

        if (images != null) {
            for (MultipartFile image : images) {
                if (image.isEmpty()) continue;

                String path = fileStorageService.storeFile(image);

                FileStore imageEntity = new FileStore();
                imageEntity.setPath(path);
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
    }
}
