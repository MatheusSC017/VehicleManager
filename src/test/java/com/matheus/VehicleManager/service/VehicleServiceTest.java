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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Test
    void testFindByChassi() {
        String chassi = "TestChassi123";
        Vehicle vehicle = new Vehicle();
        vehicle.setChassi(chassi);

        when(vehicleRepository.findByChassi(chassi)).thenReturn(vehicle);

        Vehicle foundVehicle = vehicleService.findByChassi(chassi);

        assertEquals(chassi, foundVehicle.getChassi());
        verify(vehicleRepository, times(1)).findByChassi(chassi);
    }

    @Test
    void testGetVehicleWithImagesById() {
        Long vehicleId = 1L;
        Vehicle vehicle = new Vehicle();
        vehicle.setId(vehicleId);
        vehicle.setImages(new ArrayList<>());

        when(vehicleRepository.getReferenceById(vehicleId)).thenReturn(vehicle);

        VehicleImagesResponseDTO foundVehicle = vehicleService.getVehicleWithImagesById(vehicleId);

        assertEquals(vehicleId, foundVehicle.id());
        assertEquals(new ArrayList<>(), foundVehicle.images());
        verify(vehicleRepository, times(1)).getReferenceById(vehicleId);
    }

    @Test
    void testGetFilteredVehicles() {
        Vehicle vehicle1 = new Vehicle();
        vehicle1.setModel("Ford");
        vehicle1.setBrand("Maverick");
        vehicle1.setChassi("TestChassi456");

        Vehicle vehicle2 = new Vehicle();
        vehicle2.setModel("Ford");
        vehicle2.setBrand("Mustang");
        vehicle2.setChassi("TestChassi123");

        List<Vehicle> searchedVehicles = new ArrayList<>();
        searchedVehicles.add(vehicle1);
        searchedVehicles.add(vehicle2);

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
        assertEquals("Ford", foundVehicles.getContent().get(0).getModel());
        assertEquals("Maverick", foundVehicles.getContent().get(0).getBrand());
        assertEquals("TestChassi456", foundVehicles.getContent().get(0).getChassi());
        assertEquals("Ford", foundVehicles.getContent().get(1).getModel());
        assertEquals("Mustang", foundVehicles.getContent().get(1).getBrand());
        assertEquals("TestChassi123", foundVehicles.getContent().get(1).getChassi());
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
    void tetFilteredVehiclesWithOneImage() {
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

        List<VehicleImageResponseDTO> searchedVehicles = new ArrayList<>();
        searchedVehicles.add(vehicle1);
        searchedVehicles.add(vehicle2);

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
    void testSearchAvailableVehicles() {
        Vehicle vehicle1 = new Vehicle();
        vehicle1.setModel("Ford");
        vehicle1.setBrand("Maverick");
        vehicle1.setChassi("TestChassi456");

        Vehicle vehicle2 = new Vehicle();
        vehicle2.setModel("Ford");
        vehicle2.setBrand("Mustang");
        vehicle2.setChassi("TestChassi123");

        List<Vehicle> searchedVehicles = new ArrayList<>();
        searchedVehicles.add(vehicle1);
        searchedVehicles.add(vehicle2);
        when(vehicleRepository.searchAvailableVehicles("Ford")).thenReturn(searchedVehicles);


        List<Vehicle> foundVehicles = vehicleService.searchAvailableVehicles("Ford");

        assertEquals(2, foundVehicles.size());
        assertEquals("Ford", foundVehicles.get(0).getModel());
        assertEquals("Maverick", foundVehicles.get(0).getBrand());
        assertEquals("TestChassi456", foundVehicles.get(0).getChassi());
        assertEquals("Ford", foundVehicles.get(1).getModel());
        assertEquals("Mustang", foundVehicles.get(1).getBrand());
        assertEquals("TestChassi123", foundVehicles.get(1).getChassi());
        verify(vehicleRepository, times(1)).searchAvailableVehicles("Ford");
    }

    @Test
    void testCreate() {
        VehicleRequestDTO dto = new VehicleRequestDTO();
        dto.setVehicleType(VehicleType.CAR);
        dto.setModel("Test Model");
        dto.setBrand("Test Brand");
        dto.setYear(1999);
        dto.setColor("Test Color");
        dto.setMileage(new BigDecimal("9999.99"));
        dto.setChassi("TestChassi123");
        dto.setPrice(new BigDecimal("99999.99"));
        dto.setVehicleFuel(VehicleFuel.HYBRID);
        dto.setDoors(2);

        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleType(VehicleType.CAR);
        vehicle.setModel("Test Model");
        vehicle.setBrand("Test Brand");
        vehicle.setYear(1999);
        vehicle.setColor("Test Color");
        vehicle.setMileage(new BigDecimal("9999.99"));
        vehicle.setChassi("TestChassi123");
        vehicle.setPrice(new BigDecimal("99999.99"));
        vehicle.setVehicleFuel(VehicleFuel.HYBRID);
        vehicle.setDoors(2);
        vehicle.setVehicleStatus(VehicleStatus.AVAILABLE);

        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);

        Vehicle createdVehicle = vehicleService.create(dto);

        assertEquals(VehicleType.CAR, createdVehicle.getVehicleType());
        assertEquals("Test Model", createdVehicle.getModel());
        assertEquals("Test Brand", createdVehicle.getBrand());
        assertEquals(1999, createdVehicle.getYear());
        assertEquals("Test Color", createdVehicle.getColor());
        assertEquals(null, createdVehicle.getPlate());
        assertEquals(new BigDecimal("9999.99"), createdVehicle.getMileage());
        assertEquals("TestChassi123", createdVehicle.getChassi());
        assertEquals(new BigDecimal("99999.99"), createdVehicle.getPrice());
        assertEquals(VehicleFuel.HYBRID, createdVehicle.getVehicleFuel());
        assertEquals(null, createdVehicle.getVehicleChange());
        assertEquals(2, createdVehicle.getDoors());
        assertEquals(null, createdVehicle.getMotor());
        assertEquals(null, createdVehicle.getPower());
        assertEquals(VehicleStatus.AVAILABLE, createdVehicle.getVehicleStatus());
        verify(vehicleRepository, times(1)).save(any(Vehicle.class));
    }

    @Test
    void testUpdate() {
        Long vehicleId = 1L;
        VehicleRequestDTO dto = new VehicleRequestDTO();
        dto.setVehicleType(VehicleType.MOTORCYCLE);
        dto.setModel("Updated Model");
        dto.setBrand("Updated Brand");
        dto.setYear(2000);
        dto.setColor("Updated Color");
        dto.setMileage(new BigDecimal("10000.00"));
        dto.setChassi("UpdatedChassi123");
        dto.setPrice(new BigDecimal("100000.00"));
        dto.setVehicleFuel(VehicleFuel.GASOLINE);
        dto.setDoors(0);

        Vehicle existingVehicle = new Vehicle();
        existingVehicle.setId(vehicleId);
        existingVehicle.setVehicleType(VehicleType.CAR);
        existingVehicle.setModel("Original Model");
        existingVehicle.setBrand("Original Brand");
        existingVehicle.setYear(1999);
        existingVehicle.setColor("Original Color");
        existingVehicle.setMileage(new BigDecimal("9999.99"));
        existingVehicle.setChassi("OriginalChassi123");
        existingVehicle.setPrice(new BigDecimal("99999.99"));
        existingVehicle.setVehicleFuel(VehicleFuel.HYBRID);
        existingVehicle.setDoors(2);
        existingVehicle.setVehicleStatus(VehicleStatus.AVAILABLE);

        when(vehicleRepository.getReferenceById(vehicleId)).thenReturn(existingVehicle);
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Vehicle updatedVehicle = vehicleService.update(vehicleId, dto);

        assertEquals(VehicleType.MOTORCYCLE, updatedVehicle.getVehicleType());
        assertEquals("Updated Model", updatedVehicle.getModel());
        assertEquals("Updated Brand", updatedVehicle.getBrand());
        assertEquals(2000, updatedVehicle.getYear());
        assertEquals("Updated Color", updatedVehicle.getColor());
        assertEquals(null, updatedVehicle.getPlate());
        assertEquals(new BigDecimal("10000.00"), updatedVehicle.getMileage());
        assertEquals("UpdatedChassi123", updatedVehicle.getChassi());
        assertEquals(new BigDecimal("100000.00"), updatedVehicle.getPrice());
        assertEquals(VehicleFuel.GASOLINE, updatedVehicle.getVehicleFuel());
        assertEquals(null, updatedVehicle.getVehicleChange());
        assertEquals(0, updatedVehicle.getDoors());
        assertEquals(null, updatedVehicle.getMotor());
        assertEquals(null, updatedVehicle.getPower());
        assertEquals(VehicleStatus.AVAILABLE, updatedVehicle.getVehicleStatus());
        verify(vehicleRepository, times(1)).getReferenceById(vehicleId);
        verify(vehicleRepository, times(1)).save(any(Vehicle.class));
    }

    @Test
    void testDelete() {
        Long vehicleId = 1L;
        doNothing().when(vehicleRepository).deleteById(vehicleId);
        vehicleService.delete(vehicleId);
        verify(vehicleRepository, times(1)).deleteById(vehicleId);
    }

}

