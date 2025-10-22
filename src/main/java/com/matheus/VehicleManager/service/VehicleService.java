package com.matheus.VehicleManager.service;

import com.matheus.VehicleManager.dto.FileResponseDTO;
import com.matheus.VehicleManager.dto.VehicleImageResponseDTO;
import com.matheus.VehicleManager.dto.VehicleImagesResponseDTO;
import com.matheus.VehicleManager.dto.VehicleRequestDTO;
import com.matheus.VehicleManager.enums.VehicleFuel;
import com.matheus.VehicleManager.enums.VehicleStatus;
import com.matheus.VehicleManager.enums.VehicleType;
import com.matheus.VehicleManager.model.FileStore;
import com.matheus.VehicleManager.model.Vehicle;
import com.matheus.VehicleManager.repository.FileRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.matheus.VehicleManager.repository.VehicleRepository;

import java.util.List;

@Service
public class VehicleService {

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private FileRepository fileRepository;

    @Cacheable(value = "vehicle_by_chassi", key = "#chassi")
    public Vehicle findByChassi(String chassi) {
        return vehicleRepository.findByChassi(chassi)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle with chassi " + chassi + " not found"));
    }

    @Cacheable(value = "vehicle_with_image", key = "#id")
    public VehicleImagesResponseDTO getVehicleWithImagesById(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle with id " + id + " not found"));
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
                        image.getType(),
                        image.getVehicle().getId()
                )).toList()
        );
    }

    @Cacheable(
        value = "vehicle_filtered",
        key = "#search + '-' + #status + '-' + #type + '-' + #fuel + '-' + #priceMin + '-' + #priceMax + '-' + #page + '-' + #size"
    )
    public Page<Vehicle> getFilteredVehicles(String search, String status, String type,
                                                             String fuel, int priceMin, int priceMax, int page, int size) {
        Pageable paging = PageRequest.of(page, size);
        VehicleStatus statusEnum = (status != null && !status.isEmpty()) ? VehicleStatus.valueOf(status) : null;
        VehicleType typeEnum = (type != null && !type.isEmpty()) ? VehicleType.valueOf(type) : null;
        VehicleFuel fuelEnum = (fuel != null && !fuel.isEmpty()) ? VehicleFuel.valueOf(fuel) : null;
        Integer min = priceMin > 0 ? priceMin : null;
        Integer max = priceMax > 0 ? priceMax : null;

        Page<Vehicle> vehicles = vehicleRepository.searchVehicles(search, statusEnum, typeEnum, fuelEnum, min, max, paging);
        return vehicles;
    }

    @Cacheable(
        value = "vehicle_filtered_with_image",
        key = "#search + '-' + #status + '-' + #type + '-' + #fuel + '-' + #priceMin + '-' + #priceMax + '-' + #page + '-' + #size"
    )
    public Page<VehicleImageResponseDTO> getFilteredVehiclesWithOneImage(String search, String status, String type,
                                                                         String fuel, int priceMin, int priceMax, int page, int size) {
        Pageable paging = PageRequest.of(page, size);
        VehicleStatus statusEnum = (status != null && !status.isEmpty()) ? VehicleStatus.valueOf(status) : null;
        VehicleType typeEnum = (type != null && !type.isEmpty()) ? VehicleType.valueOf(type) : null;
        VehicleFuel fuelEnum = (fuel != null && !fuel.isEmpty()) ? VehicleFuel.valueOf(fuel) : null;
        Integer min = priceMin > 0 ? priceMin : null;
        Integer max = priceMax > 0 ? priceMax : null;

        Page<VehicleImageResponseDTO> vehicles = vehicleRepository.searchVehiclesWithImages(search, statusEnum, typeEnum, fuelEnum, min, max, paging);
        return vehicles;
    }

    @Cacheable(value = "vehicle_search_available", key = "#searchFor")
    public List<Vehicle> searchAvailableVehicles(String searchFor) {
        return vehicleRepository.searchAvailableVehicles(searchFor);
    }

    @CacheEvict(value = {
            "vehicle_by_chassi", "vehicle_with_image", "vehicle_filtered", "vehicle_filtered_with_image", "vehicle_search_available"
    }, allEntries = true)
    public Vehicle create(VehicleRequestDTO vehicleDto) {
        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleType(vehicleDto.getVehicleType());
        vehicle.setModel(vehicleDto.getModel());
        vehicle.setBrand(vehicleDto.getBrand());
        vehicle.setYear(vehicleDto.getYear());
        vehicle.setColor(vehicleDto.getColor());
        vehicle.setPlate(vehicleDto.getPlate());
        vehicle.setChassi(vehicleDto.getChassi());
        vehicle.setMileage(vehicleDto.getMileage());
        vehicle.setPrice(vehicleDto.getPrice());
        vehicle.setVehicleFuel(vehicleDto.getVehicleFuel());
        vehicle.setVehicleChange(vehicleDto.getVehicleChange());
        vehicle.setDoors(vehicleDto.getDoors());
        vehicle.setMotor(vehicleDto.getMotor());
        vehicle.setPower(vehicleDto.getPower());
        return vehicleRepository.save(vehicle);
    }

    @CacheEvict(value = {
            "vehicle_by_chassi", "vehicle_with_image", "vehicle_filtered", "vehicle_filtered_with_image", "vehicle_search_available"
    }, allEntries = true)
    public Vehicle update(Long vehicleId, VehicleRequestDTO vehicleDto) {
        Vehicle vehicle = vehicleRepository.getReferenceById(vehicleId);
        vehicle.setVehicleType(vehicleDto.getVehicleType());
        vehicle.setModel(vehicleDto.getModel());
        vehicle.setBrand(vehicleDto.getBrand());
        vehicle.setYear(vehicleDto.getYear());
        vehicle.setColor(vehicleDto.getColor());
        vehicle.setPlate(vehicleDto.getPlate());
        vehicle.setChassi(vehicleDto.getChassi());
        vehicle.setMileage(vehicleDto.getMileage());
        vehicle.setPrice(vehicleDto.getPrice());
        vehicle.setVehicleFuel(vehicleDto.getVehicleFuel());
        vehicle.setVehicleChange(vehicleDto.getVehicleChange());
        vehicle.setDoors(vehicleDto.getDoors());
        vehicle.setMotor(vehicleDto.getMotor());
        vehicle.setPower(vehicleDto.getPower());
        return vehicleRepository.save(vehicle);
    }

    @CacheEvict(value = {
            "vehicle_by_chassi", "vehicle_with_image", "vehicle_filtered", "vehicle_filtered_with_image", "vehicle_search_available"
    }, allEntries = true)
    public void delete(Long id) {
        vehicleRepository.deleteById(id);
    }

}
