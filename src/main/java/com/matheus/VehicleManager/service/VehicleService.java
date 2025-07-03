package com.matheus.VehicleManager.service;

import com.matheus.VehicleManager.dto.FileResponseDTO;
import com.matheus.VehicleManager.dto.VehicleImageResponseDTO;
import com.matheus.VehicleManager.dto.VehicleImagesResponseDTO;
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

import java.util.List;

@Service
public class VehicleService {

    @Autowired
    private VehicleRepository vehicleRepository;

    public Vehicle findByChassi(String chassi) {
        return vehicleRepository.findByChassi(chassi);
    }

    public VehicleImagesResponseDTO getVehicleWithImagesById(Long id) {
        Vehicle vehicle = this.vehicleRepository.getReferenceById(id);
        return this.getVehicleWithImages(vehicle);
    }

    public Page<Vehicle> getFilteredVehicles(String search, String status, String type,
                                                             String fuel, int priceMin, int priceMax, Pageable paging) {
        VehicleStatus statusEnum = (status != null && !status.isEmpty()) ? VehicleStatus.valueOf(status) : null;
        VehicleType typeEnum = (type != null && !type.isEmpty()) ? VehicleType.valueOf(type) : null;
        VehicleFuel fuelEnum = (fuel != null && !fuel.isEmpty()) ? VehicleFuel.valueOf(fuel) : null;
        Integer min = priceMin > 0 ? priceMin : null;
        Integer max = priceMax > 0 ? priceMax : null;

        Page<Vehicle> vehicles = vehicleRepository.searchVehicles(search, statusEnum, typeEnum, fuelEnum, min, max, paging);
        return vehicles;
    }

    public Page<VehicleImageResponseDTO> getFilteredVehiclesWithOneImage(String search, String status, String type,
                                                                         String fuel, int priceMin, int priceMax, Pageable paging) {
        VehicleStatus statusEnum = (status != null && !status.isEmpty()) ? VehicleStatus.valueOf(status) : null;
        VehicleType typeEnum = (type != null && !type.isEmpty()) ? VehicleType.valueOf(type) : null;
        VehicleFuel fuelEnum = (fuel != null && !fuel.isEmpty()) ? VehicleFuel.valueOf(fuel) : null;
        Integer min = priceMin > 0 ? priceMin : null;
        Integer max = priceMax > 0 ? priceMax : null;

        Page<VehicleImageResponseDTO> vehicles = vehicleRepository.searchVehiclesWithFilters(search, statusEnum, typeEnum, fuelEnum, min, max, paging);
        return vehicles;
    }

    private VehicleImagesResponseDTO getVehicleWithImages(Vehicle vehicle) {
        List<FileStore> images = vehicle.getImages();
        return new VehicleImagesResponseDTO(
                vehicle.getId(),
                vehicle.getVehicleType(),
                vehicle.getVehicleStatus(),
                vehicle.getModel(),
                vehicle.getBrand(),
                vehicle.getYear(),
                vehicle.getColor(),
                vehicle.getPlate(),
                vehicle.getChassi(),
                vehicle.getMileage(),
                vehicle.getPrice(),
                vehicle.getVehicleFuel(),
                vehicle.getVehicleChange(),
                vehicle.getDoors(),
                vehicle.getMotor(),
                vehicle.getPower(),
                images.stream().map(image -> new FileResponseDTO(
                        image.getId(),
                        image.getPath(),
                        image.getType()
                )).toList()
        );
    }

}
