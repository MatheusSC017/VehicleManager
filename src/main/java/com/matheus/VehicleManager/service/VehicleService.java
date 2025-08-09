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

    @Autowired
    private FileRepository fileRepository;

    public Vehicle findByChassi(String chassi) {
        return vehicleRepository.findByChassi(chassi)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle with chassi " + chassi + " not found"));
    }

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

        Page<VehicleImageResponseDTO> vehicles = vehicleRepository.searchVehiclesWithImages(search, statusEnum, typeEnum, fuelEnum, min, max, paging);
        return vehicles;
    }

    public List<Vehicle> searchAvailableVehicles(String searchFor) {
        return vehicleRepository.searchAvailableVehicles(searchFor);
    }

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

    public Vehicle update(Long id, VehicleRequestDTO vehicleDto) {
        Vehicle vehicle = vehicleRepository.getReferenceById(id);
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

    public void delete(Long id) {
        vehicleRepository.deleteById(id);
    }

}
