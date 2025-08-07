package com.matheus.VehicleManager.service;

import com.matheus.VehicleManager.dto.VehicleImageResponseDTO;
import com.matheus.VehicleManager.dto.VehicleImagesResponseDTO;
import com.matheus.VehicleManager.dto.VehicleRequestDTO;
import com.matheus.VehicleManager.dto.VehicleResponseDTO;
import com.matheus.VehicleManager.enums.VehicleChange;
import com.matheus.VehicleManager.enums.VehicleFuel;
import com.matheus.VehicleManager.enums.VehicleStatus;
import com.matheus.VehicleManager.enums.VehicleType;
import com.matheus.VehicleManager.model.Vehicle;
import com.matheus.VehicleManager.repository.VehicleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private VehicleService vehicleService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private Vehicle buildVehicle(Long id, VehicleStatus status) {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(id);
        vehicle.setChassi("TestChassi123");
        vehicle.setBrand("TestBrand");
        vehicle.setModel("TestModel");
        vehicle.setVehicleStatus(status);
        vehicle.setVehicleType(VehicleType.CAR);
        vehicle.setYear(1999);
        vehicle.setColor("Test Color");
        vehicle.setMileage(new BigDecimal("9999.99"));
        vehicle.setPrice(new BigDecimal("99999.99"));
        vehicle.setVehicleFuel(VehicleFuel.HYBRID);
        vehicle.setDoors(2);
        return vehicle;
    }

    @Test
    @DisplayName("Should return a specific vehicle based on the chassi value")
    void testFindByChassi() {
        Vehicle vehicle = buildVehicle(1L, VehicleStatus.AVAILABLE);

        when(vehicleRepository.findByChassi("TestChassi123")).thenReturn(Optional.of(vehicle));
        Vehicle foundVehicle = vehicleService.findByChassi("TestChassi123");

        assertEquals("TestChassi123", foundVehicle.getChassi());
        verify(vehicleRepository, times(1)).findByChassi(anyString());
    }

    @Test
    @DisplayName("Should return a specific vehicle with the images")
    void testGetVehicleWithImagesById() {
        Vehicle vehicle = buildVehicle(1L, VehicleStatus.AVAILABLE);
        vehicle.setImages(new ArrayList<>());

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        VehicleImagesResponseDTO foundVehicle = vehicleService.getVehicleWithImagesById(1L);

        assertEquals(1L, foundVehicle.id());
        assertEquals(new ArrayList<>(), foundVehicle.images());
        verify(vehicleRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when getting a vehicle with a non-existent ID")
    void testGetVehicleWithImagesByIdNotFound() {
        when(vehicleRepository.getReferenceById(anyLong())).thenThrow(EntityNotFoundException.class);
        assertThrows(EntityNotFoundException.class, () -> vehicleService.getVehicleWithImagesById(99L));
    }

    @Test
    @DisplayName("Should return all vehicles with pagination")
    void testGetFilteredVehicles() {
        Vehicle vehicle1 = buildVehicle(1L, VehicleStatus.AVAILABLE);
        Vehicle vehicle2 = buildVehicle(2L, VehicleStatus.AVAILABLE);
        List<Vehicle> searchedVehicles = List.of(vehicle1, vehicle2);

        Pageable paging = PageRequest.of(0, 20);
        Page<Vehicle> vehiclePage = new PageImpl<>(searchedVehicles, paging, searchedVehicles.size());
        when(vehicleRepository.searchVehicles(
            anyString(),
            any(),
            any(),
            any(),
            isNull(),
            isNull(),
            any(Pageable.class)
        )).thenReturn(vehiclePage);

        Page<Vehicle> foundVehicles = vehicleService.getFilteredVehicles("", null, null, null, 0, 0, paging);

        assertEquals(2, foundVehicles.getContent().size());
        assertEquals(vehicle1, foundVehicles.getContent().get(0));
        assertEquals(vehicle2, foundVehicles.getContent().get(1));
        verify(vehicleRepository, times(1)).searchVehicles(
            anyString(),
            any(),
            any(),
            any(),
            isNull(),
            isNull(),
            any(Pageable.class)
        );
    }

    @Test
    @DisplayName("Should return filtered vehicles by status")
    void testGetFilteredVehiclesByStatus() {
        Vehicle vehicle1 = buildVehicle(1L, VehicleStatus.AVAILABLE);
        Vehicle vehicle2 = buildVehicle(2L, VehicleStatus.SOLD);
        List<Vehicle> searchedVehicles = List.of(vehicle1, vehicle2);

        Pageable paging = PageRequest.of(0, 20);
        Page<Vehicle> vehiclePage = new PageImpl<>(searchedVehicles, paging, searchedVehicles.size());

        when(vehicleRepository.searchVehicles(
                anyString(),
                eq(VehicleStatus.AVAILABLE),
                any(),
                any(),
                isNull(),
                isNull(),
                any(Pageable.class)
        )).thenReturn(vehiclePage);

        Page<Vehicle> foundVehicles = vehicleService.getFilteredVehicles("", "AVAILABLE", null, null, 0, 0, paging);

        assertEquals(2, foundVehicles.getContent().size());
        assertEquals(VehicleStatus.AVAILABLE, foundVehicles.getContent().get(0).getVehicleStatus());
    }

    @Test
    @DisplayName("Should return all vehicles with pagination and one image by register")
    void testGetFilteredVehiclesWithOneImage() {
        VehicleImageResponseDTO vehicle1 = new VehicleImageResponseDTO(
            1L, VehicleType.CAR, VehicleStatus.AVAILABLE, "Ford", "Maverick", 2000, "Red",
            "ABC1234", "TestChassi456", new BigDecimal(0), new BigDecimal(15000), VehicleFuel.GASOLINE,
            VehicleChange.AUTOMATIC, 4, "2.0", "120cv", "TestMaverickUrlImage"
        );

        VehicleImageResponseDTO vehicle2 = new VehicleImageResponseDTO(
            2L, VehicleType.CAR, VehicleStatus.MAINTENANCE, "Ford", "Mustang", 1999, "Blue",
            "DEF5678", "TestChassi123", new BigDecimal(10000), new BigDecimal(25000), VehicleFuel.HYBRID,
            VehicleChange.AUTOMATED, 3, "1.5", "150cv", "TestMustangUrlImage"
        );

        List<VehicleImageResponseDTO> searchedVehicles = List.of(vehicle1, vehicle2);

        Pageable paging = PageRequest.of(0, 20);
        Page<VehicleImageResponseDTO> vehiclePage = new PageImpl<>(searchedVehicles, paging, searchedVehicles.size());
        when(vehicleRepository.searchVehiclesWithImages(
            anyString(),
            any(),
            any(),
            any(),
            isNull(),
            isNull(),
            any(Pageable.class)
        )).thenReturn(vehiclePage);


        Page<VehicleImageResponseDTO> foundVehicles = vehicleService.getFilteredVehiclesWithOneImage("", null, null, null, 0, 0, paging);

        assertEquals(2, foundVehicles.getContent().size());
        assertEquals(1L, foundVehicles.getContent().get(0).id());
        assertEquals("Ford", foundVehicles.getContent().get(0).model());
        assertEquals("Maverick", foundVehicles.getContent().get(0).brand());
        assertEquals(VehicleType.CAR, foundVehicles.getContent().get(0).vehicleType());
        assertEquals(VehicleStatus.AVAILABLE, foundVehicles.getContent().get(0).vehicleStatus());
        assertEquals(2000, foundVehicles.getContent().get(0).year());
        assertEquals("Red", foundVehicles.getContent().get(0).color());
        assertEquals("ABC1234", foundVehicles.getContent().get(0).plate());
        assertEquals("TestChassi456", foundVehicles.getContent().get(0).chassi());
        assertEquals(new BigDecimal(0), foundVehicles.getContent().get(0).mileage());
        assertEquals(new BigDecimal(15000), foundVehicles.getContent().get(0).price());
        assertEquals(VehicleFuel.GASOLINE, foundVehicles.getContent().get(0).vehicleFuel());
        assertEquals(VehicleChange.AUTOMATIC, foundVehicles.getContent().get(0).vehicleChange());
        assertEquals(4, foundVehicles.getContent().get(0).doors());
        assertEquals("2.0", foundVehicles.getContent().get(0).motor());
        assertEquals("120cv", foundVehicles.getContent().get(0).power());
        assertEquals("TestMaverickUrlImage", foundVehicles.getContent().get(0).image());

        assertEquals(2L, foundVehicles.getContent().get(1).id());
        assertEquals("Ford", foundVehicles.getContent().get(1).model());
        assertEquals("Mustang", foundVehicles.getContent().get(1).brand());
        assertEquals(VehicleType.CAR, foundVehicles.getContent().get(1).vehicleType());
        assertEquals(VehicleStatus.MAINTENANCE, foundVehicles.getContent().get(1).vehicleStatus());
        assertEquals(1999, foundVehicles.getContent().get(1).year());
        assertEquals("Blue", foundVehicles.getContent().get(1).color());
        assertEquals("DEF5678", foundVehicles.getContent().get(1).plate());
        assertEquals("TestChassi123", foundVehicles.getContent().get(1).chassi());
        assertEquals(new BigDecimal(10000), foundVehicles.getContent().get(1).mileage());
        assertEquals(new BigDecimal(25000), foundVehicles.getContent().get(1).price());
        assertEquals(VehicleFuel.HYBRID, foundVehicles.getContent().get(1).vehicleFuel());
        assertEquals(VehicleChange.AUTOMATED, foundVehicles.getContent().get(1).vehicleChange());
        assertEquals(3, foundVehicles.getContent().get(1).doors());
        assertEquals("1.5", foundVehicles.getContent().get(1).motor());
        assertEquals("150cv", foundVehicles.getContent().get(1).power());
        assertEquals("TestMustangUrlImage", foundVehicles.getContent().get(1).image());
        verify(vehicleRepository, times(1)).searchVehiclesWithImages(
                anyString(),
                any(),
                any(),
                any(),
                isNull(),
                isNull(),
                any(Pageable.class)
        );
    }

    @Test
    @DisplayName("Should return all available vehicles")
    void testSearchAvailableVehicles() {
        Vehicle vehicle1 = buildVehicle(1L, VehicleStatus.AVAILABLE);
        Vehicle vehicle2 = buildVehicle(2L, VehicleStatus.AVAILABLE);
        List<Vehicle> searchedVehicles = List.of(vehicle1, vehicle2);
        when(vehicleRepository.searchAvailableVehicles("Ford")).thenReturn(searchedVehicles);

        List<Vehicle> foundVehicles = vehicleService.searchAvailableVehicles("Ford");

        assertEquals(2, foundVehicles.size());
        assertEquals(vehicle1, foundVehicles.get(0));
        assertEquals(vehicle2, foundVehicles.get(1));
        verify(vehicleRepository, times(1)).searchAvailableVehicles("Ford");
    }

    @Test
    @DisplayName("Should create a new vehicle")
    void testCreate() {
        VehicleRequestDTO vehicleRequestDTO = new VehicleRequestDTO();
        vehicleRequestDTO.setVehicleType(VehicleType.CAR);
        vehicleRequestDTO.setModel("Test Model");
        vehicleRequestDTO.setBrand("Test Brand");
        vehicleRequestDTO.setYear(1999);
        vehicleRequestDTO.setColor("Test Color");
        vehicleRequestDTO.setMileage(new BigDecimal("9999.99"));
        vehicleRequestDTO.setChassi("TestChassi123");
        vehicleRequestDTO.setPrice(new BigDecimal("99999.99"));
        vehicleRequestDTO.setVehicleFuel(VehicleFuel.HYBRID);
        vehicleRequestDTO.setDoors(2);

        Vehicle vehicle = buildVehicle(1L, VehicleStatus.AVAILABLE);

        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);
        Vehicle createdVehicle = vehicleService.create(vehicleRequestDTO);

        assertEquals(vehicle, createdVehicle);
        verify(vehicleRepository, times(1)).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("Should update a specific vehicle")
    void testUpdate() {
        VehicleRequestDTO vehicleRequestDTO = new VehicleRequestDTO();
        vehicleRequestDTO.setModel("Updated Model");
        vehicleRequestDTO.setBrand("Updated Brand");
        vehicleRequestDTO.setYear(2024);
        vehicleRequestDTO.setColor("Blue");
        vehicleRequestDTO.setPlate("NEW123");
        vehicleRequestDTO.setChassi("NEWCHASSI");
        vehicleRequestDTO.setMileage(new BigDecimal("100.0"));
        vehicleRequestDTO.setPrice(new BigDecimal("50000.0"));
        vehicleRequestDTO.setVehicleType(VehicleType.CAR);
        vehicleRequestDTO.setVehicleFuel(VehicleFuel.ELECTRIC);
        vehicleRequestDTO.setVehicleChange(VehicleChange.AUTOMATIC);
        vehicleRequestDTO.setDoors(4);
        vehicleRequestDTO.setMotor("Electric Motor");
        vehicleRequestDTO.setPower("300hp");

        Vehicle vehicle = buildVehicle(1L, VehicleStatus.AVAILABLE);

        when(vehicleRepository.getReferenceById(1L)).thenReturn(vehicle);
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Vehicle updatedVehicle = vehicleService.update(1L, vehicleRequestDTO);

        assertEquals("Updated Model", updatedVehicle.getModel());
        assertEquals("Updated Brand", updatedVehicle.getBrand());
        assertEquals(2024, updatedVehicle.getYear());
        assertEquals("Blue", updatedVehicle.getColor());
        assertEquals("NEW123", updatedVehicle.getPlate());
        assertEquals("NEWCHASSI", updatedVehicle.getChassi());
        assertEquals(0, new BigDecimal("100.0").compareTo(updatedVehicle.getMileage()));
        assertEquals(0, new BigDecimal("50000.0").compareTo(updatedVehicle.getPrice()));
        assertEquals(VehicleType.CAR, updatedVehicle.getVehicleType());
        assertEquals(VehicleFuel.ELECTRIC, updatedVehicle.getVehicleFuel());
        assertEquals(VehicleChange.AUTOMATIC, updatedVehicle.getVehicleChange());
        assertEquals(4, updatedVehicle.getDoors());
        assertEquals("Electric Motor", updatedVehicle.getMotor());
        assertEquals("300hp", updatedVehicle.getPower());
        verify(vehicleRepository, times(1)).getReferenceById(1L);
        verify(vehicleRepository, times(1)).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when updating a non-existent vehicle")
    void testUpdateNotFound() {
        VehicleRequestDTO dto = new VehicleRequestDTO();
        when(vehicleRepository.getReferenceById(anyLong())).thenThrow(EntityNotFoundException.class);
        assertThrows(EntityNotFoundException.class, () -> vehicleService.update(99L, dto));
    }

    @Test
    @DisplayName("Should delete a specific vehicle")
    void testDelete() {
        doNothing().when(vehicleRepository).deleteById(1L);
        vehicleService.delete(1L);
        verify(vehicleRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw EmptyResultDataAccessException when deleting a non-existent vehicle")
    void testDeleteNotFound() {
        doThrow(EmptyResultDataAccessException.class).when(vehicleRepository).deleteById(anyLong());
        assertThrows(EmptyResultDataAccessException.class, () -> vehicleService.delete(99L));
    }

}

