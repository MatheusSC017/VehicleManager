package com.matheus.VehicleManager.service;

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

    public VehicleWithOneImageDTO getVehicleWithImageById(Long id) {
        Vehicle vehicle = this.vehicleRepository.getReferenceById(id);
        return this.getVehicleWithOneImage(vehicle);
    }

    public List<VehicleWithOneImageDTO> getVehiclesWithOneImage() {
        List<Vehicle> vehicles = this.vehicleRepository.findAll();
        return this.getVehiclesImages(vehicles);
    }

    public List<VehicleWithOneImageDTO> getByBrandAndModelIgnoreCaseWithOneImage(String search) {
        List<Vehicle> vehicles = this.vehicleRepository.findByBrandAndModelIgnoreCase(search);
        return this.getVehiclesImages(vehicles);
    }

    public List<VehicleWithOneImageDTO> getFilteredVehiclesWithOneImage(String status, String type, String fuel, int priceMin, int priceMax) {
        List<Vehicle> vehicles = vehicleRepository.findAll();
        vehicles = vehicles.stream()
                .filter(v -> status.isEmpty() || v.getVehicleStatus().name().equalsIgnoreCase(status))
                .filter(v -> type.isEmpty() || v.getVehicleType().name().equalsIgnoreCase(type))
                .filter(v -> fuel.isEmpty() || v.getVehicleFuel().name().equalsIgnoreCase(fuel))
                .filter(v -> priceMin == 0 || v.getPrice().compareTo(BigDecimal.valueOf(priceMin)) >= 0)
                .filter(v -> priceMax == 0 || v.getPrice().compareTo(BigDecimal.valueOf(priceMax)) <= 0)
                .toList();
        return this.getVehiclesImages(vehicles);
    }

    private List<VehicleWithOneImageDTO> getVehiclesImages(List<Vehicle> vehicles) {
        List<VehicleWithOneImageDTO> vehiclesWithImage = new ArrayList<>();

        for (Vehicle vehicle : vehicles) {
            vehiclesWithImage.add(this.getVehicleWithOneImage(vehicle));
        }

        return vehiclesWithImage;
    }

    private VehicleWithOneImageDTO getVehicleWithOneImage(Vehicle vehicle) {
        Optional<FileStore> image = vehicle.getImages().stream()
                .filter(img -> img.getType().getFileType().equalsIgnoreCase(FileType.IMAGE.getFileType()))
                .findFirst();
        return new VehicleWithOneImageDTO(vehicle, image.orElse(null));
    }

}
