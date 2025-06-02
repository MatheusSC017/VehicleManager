package com.matheus.VehicleManager.service;

import com.matheus.VehicleManager.dto.VehicleWithImagesDTO;
import com.matheus.VehicleManager.dto.VehicleWithOneImageDTO;
import com.matheus.VehicleManager.enums.FileType;
import com.matheus.VehicleManager.model.FileStore;
import com.matheus.VehicleManager.model.Vehicle;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.matheus.VehicleManager.repository.VehicleRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class VehicleService {

    @Autowired
    private VehicleRepository vehicleRepository;

    public VehicleWithImagesDTO getVehicleWithImagesById(Long id) {
        Vehicle vehicle = this.vehicleRepository.getReferenceById(id);
        return this.getVehicleWithImages(vehicle);
    }

    public List<VehicleWithOneImageDTO> getVehiclesWithOneImage() {
        List<Vehicle> vehicles = this.vehicleRepository.findAll();
        return this.getVehiclesImage(vehicles);
    }

    public List<VehicleWithOneImageDTO> getFilteredVehiclesWithOneImage(String search,String status, String type,
                                                                        String fuel, int priceMin, int priceMax) {
        List<Vehicle> vehicles = vehicleRepository.findByBrandAndModelIgnoreCase(search);
        System.out.println(vehicles);
        vehicles = vehicles.stream()
                .filter(v -> status.isEmpty() || v.getVehicleStatus().name().equalsIgnoreCase(status))
                .filter(v -> type.isEmpty() || v.getVehicleType().name().equalsIgnoreCase(type))
                .filter(v -> fuel.isEmpty() || v.getVehicleFuel().name().equalsIgnoreCase(fuel))
                .filter(v -> priceMin == 0 || v.getPrice().compareTo(BigDecimal.valueOf(priceMin)) >= 0)
                .filter(v -> priceMax == 0 || v.getPrice().compareTo(BigDecimal.valueOf(priceMax)) <= 0)
                .toList();
        return this.getVehiclesImage(vehicles);
    }

    private List<VehicleWithOneImageDTO> getVehiclesImage(List<Vehicle> vehicles) {
        List<VehicleWithOneImageDTO> vehiclesWithOneImage = new ArrayList<>();

        for (Vehicle vehicle : vehicles) {
            vehiclesWithOneImage.add(this.getVehicleWithOneImage(vehicle));
        }

        return vehiclesWithOneImage;
    }

    private VehicleWithOneImageDTO getVehicleWithOneImage(Vehicle vehicle) {
        Optional<FileStore> image = vehicle.getImages().stream()
                .filter(img -> img.getType().getFileType().equalsIgnoreCase(FileType.IMAGE.getFileType()))
                .findFirst();
        return new VehicleWithOneImageDTO(vehicle, image.orElse(null));
    }

    private VehicleWithImagesDTO getVehicleWithImages(Vehicle vehicle) {
        List<FileStore> images = vehicle.getImages();
        return new VehicleWithImagesDTO(vehicle, images);
    }

}
