package com.matheus.VehicleManager.service;

import com.matheus.VehicleManager.dto.VehicleImageDTO;
import com.matheus.VehicleManager.dto.VehicleImagesDTO;
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

    public VehicleImagesDTO getVehicleWithImagesById(Long id) {
        Vehicle vehicle = this.vehicleRepository.getReferenceById(id);
        return this.getVehicleWithImages(vehicle);
    }

    public Page<VehicleImageDTO> getFilteredVehiclesWithOneImage(String search, String status, String type,
                                                                        String fuel, int priceMin, int priceMax, Pageable paging) {
        VehicleStatus statusEnum = (status != null && !status.isEmpty()) ? VehicleStatus.valueOf(status) : null;
        VehicleType typeEnum = (type != null && !type.isEmpty()) ? VehicleType.valueOf(type) : null;
        VehicleFuel fuelEnum = (fuel != null && !fuel.isEmpty()) ? VehicleFuel.valueOf(fuel) : null;
        Integer min = priceMin > 0 ? priceMin : null;
        Integer max = priceMax > 0 ? priceMax : null;

        Page<VehicleImageDTO> vehicles = vehicleRepository.searchVehiclesWithFilters(search, statusEnum, typeEnum, fuelEnum, min, max, paging);
        return vehicles;
    }

    private VehicleImagesDTO getVehicleWithImages(Vehicle vehicle) {
        List<FileStore> images = vehicle.getImages();
        return new VehicleImagesDTO(vehicle, images);
    }

}
