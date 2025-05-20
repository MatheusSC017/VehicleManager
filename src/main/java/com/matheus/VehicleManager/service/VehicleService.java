package com.matheus.VehicleManager.service;

import com.matheus.VehicleManager.dto.VehicleWithOneImageDTO;
import com.matheus.VehicleManager.enums.FileType;
import com.matheus.VehicleManager.model.FileStore;
import com.matheus.VehicleManager.model.Vehicle;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.matheus.VehicleManager.repository.VehicleRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class VehicleService {

    @Autowired
    private VehicleRepository vehicleRepository;

    public List<VehicleWithOneImageDTO> getVehiclesWithOneImage() {
        List<Vehicle> vehicles = this.vehicleRepository.findAll();
        return this.getVehiclesImages(vehicles);
    }

    public List<VehicleWithOneImageDTO> getByBrandAndModelIgnoreCaseWithOneImage(String search) {
        List<Vehicle> vehicles = this.vehicleRepository.findByBrandAndModelIgnoreCase(search);
        return this.getVehiclesImages(vehicles);
    }

    public List<VehicleWithOneImageDTO> getVehiclesImages(List<Vehicle> vehicles) {
        List<VehicleWithOneImageDTO> vehiclesWithImage = new ArrayList<>();

        for (Vehicle vehicle : vehicles) {
            Optional<FileStore> image = vehicle.getImages().stream()
                    .filter(img -> img.getType().getFileType().equalsIgnoreCase(FileType.IMAGE.getFileType()))
                    .findFirst();

            vehiclesWithImage.add(new VehicleWithOneImageDTO(vehicle, image.orElse(null)));
        }

        return vehiclesWithImage;
    }

}
