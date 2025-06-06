package com.matheus.VehicleManager.service;

import com.matheus.VehicleManager.dto.VehicleWithImagesDTO;
import com.matheus.VehicleManager.dto.VehicleWithOneImageDTO;
import com.matheus.VehicleManager.enums.FileType;
import com.matheus.VehicleManager.enums.VehicleFuel;
import com.matheus.VehicleManager.enums.VehicleStatus;
import com.matheus.VehicleManager.enums.VehicleType;
import com.matheus.VehicleManager.model.FileStore;
import com.matheus.VehicleManager.model.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public Page<VehicleWithOneImageDTO> getFilteredVehiclesWithOneImage(String search, String status, String type,
                                                                        String fuel, int priceMin, int priceMax, Pageable paging) {
        VehicleStatus statusEnum = (status != null && !status.isEmpty()) ? VehicleStatus.valueOf(status) : null;
        VehicleType typeEnum = (type != null && !type.isEmpty()) ? VehicleType.valueOf(type) : null;
        VehicleFuel fuelEnum = (fuel != null && !fuel.isEmpty()) ? VehicleFuel.valueOf(fuel) : null;
        Integer min = priceMin > 0 ? priceMin : null;
        Integer max = priceMax > 0 ? priceMax : null;

        Page<Vehicle> vehicles = vehicleRepository.searchVehiclesWithFilters(search, statusEnum, typeEnum, fuelEnum, min, max, paging);
        return vehicles.map(this::getVehicleWithOneImage);
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
