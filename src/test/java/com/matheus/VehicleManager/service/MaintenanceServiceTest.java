package com.matheus.VehicleManager.service;

import com.matheus.VehicleManager.enums.VehicleStatus;
import com.matheus.VehicleManager.exception.InvalidRequestException;
import com.matheus.VehicleManager.model.Maintenance;
import com.matheus.VehicleManager.model.Vehicle;
import com.matheus.VehicleManager.repository.MaintenanceRepository;
import com.matheus.VehicleManager.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class MaintenanceServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private MaintenanceRepository maintenanceRepository;

    @InjectMocks
    private MaintenanceService maintenanceService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private Vehicle buildVehicle(Long id, VehicleStatus status) {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(id);
        vehicle.setChassi("TestChassi");
        vehicle.setBrand("TestBrand");
        vehicle.setModel("TestModel");
        vehicle.setVehicleStatus(status);
        return vehicle;
    }

    private Maintenance buildMaintenance(Long maintenanceId, Vehicle vehicle) {
        Maintenance maintenance = new Maintenance();
        maintenance.setId(maintenanceId);
        maintenance.setVehicle(vehicle);
        maintenance.setAdditionalInfo("TestAdditionalInfo");
        maintenance.setStartDate(LocalDate.now().minusDays(4));
        return maintenance;
    }

    @Test
    @DisplayName("Should return all maintenances registers with pagination")
    void testFindAll() {
        Maintenance maintenance1 = buildMaintenance(1L, new Vehicle());
        Maintenance maintenance2 = buildMaintenance(2L, new Vehicle());
        List<Maintenance> maintenances = List.of(maintenance1, maintenance2);

        Pageable paging = PageRequest.of(0, 20);
        Page<Maintenance> maintenancesPage = new PageImpl<>(maintenances, paging, maintenances.size());
        when(maintenanceRepository.findAll(any(Pageable.class))).thenReturn(maintenancesPage);

        Page<Maintenance> foundMaintenances = maintenanceService.findAll(0, 20);

        assertEquals(2, foundMaintenances.getContent().size());
        assertEquals(maintenance1, foundMaintenances.getContent().get(0));
        assertEquals(maintenance2, foundMaintenances.getContent().get(1));
        verify(maintenanceRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Should return all maintenances registers based on a vehicle id")
    void testFindAllByVehicleId() {
        Long vehicleId = 1L;
        Vehicle vehicle = new Vehicle();
        vehicle.setId(vehicleId);

        Maintenance maintenance1 = buildMaintenance(1L, vehicle);
        Maintenance maintenance2 = buildMaintenance(2L, vehicle);
        List<Maintenance> maintenances = List.of(maintenance1, maintenance2);

        when(maintenanceRepository.findByVehicleIdOrderByIdDesc(vehicleId)).thenReturn(maintenances);

        List<Maintenance> foundMaintenances = maintenanceService.findAllByVehicleId(vehicleId);

        assertEquals(2, foundMaintenances.size());
        assertEquals(maintenance1, foundMaintenances.get(0));
        assertEquals(vehicleId, foundMaintenances.get(0).getVehicle().getId());
        assertEquals(maintenance2, foundMaintenances.get(1));
        assertEquals(vehicleId, foundMaintenances.get(1).getVehicle().getId());
        verify(maintenanceRepository, times(1)).findByVehicleIdOrderByIdDesc(vehicleId);
    }

    @Test
    @DisplayName("Should return a specific maintenance based on id")
    void testFindById() {
        Maintenance maintenance = buildMaintenance(1L, new Vehicle());

        when(maintenanceRepository.findById(1L)).thenReturn(Optional.of(maintenance));
        Maintenance foundMaintenance = maintenanceService.findById(1L);

        assertEquals(maintenance, foundMaintenance);
        verify(maintenanceRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should create a new maintenance and update the vehicle status")
    void testCreate() {
        Vehicle vehicle = buildVehicle(1L, VehicleStatus.AVAILABLE);
        Maintenance maintenance = buildMaintenance(1L, vehicle);

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);
        when(maintenanceRepository.save(any(Maintenance.class))).thenReturn(maintenance);
        Maintenance createdMaintenance = maintenanceService.create(1L, "TestAdditionalInfo");

        ArgumentCaptor<Vehicle> vehicleCaptor = ArgumentCaptor.forClass(Vehicle.class);
        verify(vehicleRepository).save(vehicleCaptor.capture());
        assertEquals(VehicleStatus.MAINTENANCE, vehicleCaptor.getValue().getVehicleStatus());
        assertEquals(maintenance, createdMaintenance);
        assertEquals(vehicle, createdMaintenance.getVehicle());
        verify(vehicleRepository, times(1)).findById(1L);
        verify(vehicleRepository, times(1)).save(any(Vehicle.class));
        verify(maintenanceRepository, times(1)).save(any(Maintenance.class));
    }

    @Test
    @DisplayName("Should throw an error if the vehicle requested to the maintenance creation doesn't exist")
    void testCreateVehicleNotFound() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(InvalidRequestException.class, () -> maintenanceService.create(1L, "TestAdditionalInfo"));

        verify(vehicleRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw an error if the vehicle requested to the maintenance creation doesn't available")
    void testCreateVehicleNotAvailable() {
        Vehicle vehicle = buildVehicle(1L, VehicleStatus.SOLD);

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        assertThrows(InvalidRequestException.class, () -> maintenanceService.create(1L, "TestAdditionalInfo"));

        verify(vehicleRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should update a maintenance end date and update the vehicle status")
    void testDelete() {
        Vehicle vehicle = buildVehicle(1L, VehicleStatus.MAINTENANCE);
        Maintenance maintenance = buildMaintenance(1L, vehicle);

        when(maintenanceRepository.getReferenceById(1L)).thenReturn(maintenance);
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);
        when(maintenanceRepository.save(any(Maintenance.class))).thenReturn(maintenance);

        maintenanceService.delete(1L);

        ArgumentCaptor<Vehicle> vehicleCaptor = ArgumentCaptor.forClass(Vehicle.class);
        verify(vehicleRepository).save(vehicleCaptor.capture());
        assertEquals(VehicleStatus.AVAILABLE, vehicleCaptor.getValue().getVehicleStatus());
        assertEquals(LocalDate.now(), maintenance.getEndDate());
        verify(maintenanceRepository, times(1)).getReferenceById(1L);
        verify(vehicleRepository, times(1)).save(any(Vehicle.class));
        verify(maintenanceRepository, times(1)).save(any(Maintenance.class));
    }

}
