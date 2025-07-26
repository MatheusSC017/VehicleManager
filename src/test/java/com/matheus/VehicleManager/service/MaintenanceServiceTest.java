package com.matheus.VehicleManager.service;

import com.matheus.VehicleManager.enums.VehicleStatus;
import com.matheus.VehicleManager.model.Maintenance;
import com.matheus.VehicleManager.model.Vehicle;
import com.matheus.VehicleManager.repository.MaintenanceRepository;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Test
    void testFindAll() {
        Maintenance maintenance1 = new Maintenance();
        maintenance1.setId(1L);
        maintenance1.setVehicle(new Vehicle());
        maintenance1.setAdditionalInfo("TestAdditionalInfo");
        maintenance1.setStartDate(LocalDate.now().minusDays(8));
        maintenance1.setEndDate(LocalDate.now().minusDays(4));

        Maintenance maintenance2 = new Maintenance();
        maintenance2.setId(1L);
        maintenance2.setVehicle(new Vehicle());
        maintenance2.setAdditionalInfo("TestAdditionalInfo2");
        maintenance2.setStartDate(LocalDate.now());

        List<Maintenance> maintenances = new ArrayList<>();
        maintenances.add(maintenance1);
        maintenances.add(maintenance2);

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
    void testFindAllByVehicleId() {
        Long vehicleId = 1L;
        Vehicle vehicle = new Vehicle();
        vehicle.setId(vehicleId);

        Maintenance maintenance1 = new Maintenance();
        maintenance1.setId(1L);
        maintenance1.setVehicle(vehicle);
        maintenance1.setAdditionalInfo("TestAdditionalInfo");
        maintenance1.setStartDate(LocalDate.now().minusDays(8));
        maintenance1.setEndDate(LocalDate.now().minusDays(4));

        Maintenance maintenance2 = new Maintenance();
        maintenance2.setId(1L);
        maintenance2.setVehicle(vehicle);
        maintenance2.setAdditionalInfo("TestAdditionalInfo2");
        maintenance2.setStartDate(LocalDate.now());

        List<Maintenance> maintenances = new ArrayList<>();
        maintenances.add(maintenance1);
        maintenances.add(maintenance2);

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
    void testFindById() {
        Long maintenanceId = 1L;

        Maintenance maintenance = new Maintenance();
        maintenance.setId(maintenanceId);
        maintenance.setVehicle(new Vehicle());
        maintenance.setAdditionalInfo("TestAdditionalInfo");
        maintenance.setStartDate(LocalDate.now().minusDays(8));
        maintenance.setEndDate(LocalDate.now().minusDays(4));

        when(maintenanceRepository.getReferenceById(maintenanceId)).thenReturn(maintenance);

        Maintenance foundMaintenance = maintenanceService.findById(maintenanceId);

        assertEquals(maintenance, foundMaintenance);
        verify(maintenanceRepository, times(1)).getReferenceById(maintenanceId);
    }

    @Test
    void testCreate() {
        Long vehicleId = 1L;
        String additionalInfo = "TestAdditionalInfo";

        Vehicle vehicle = new Vehicle();
        vehicle.setId(vehicleId);
        vehicle.setVehicleStatus(VehicleStatus.AVAILABLE);

        Maintenance maintenance = new Maintenance();
        maintenance.setId(1L);
        maintenance.setVehicle(vehicle);
        maintenance.setAdditionalInfo(additionalInfo);
        maintenance.setStartDate(LocalDate.now());

        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);
        when(maintenanceRepository.save(any(Maintenance.class))).thenReturn(maintenance);

        Maintenance createdMaintenance = maintenanceService.create(vehicleId, additionalInfo);

        assertEquals(maintenance, createdMaintenance);
        assertEquals(LocalDate.now(), createdMaintenance.getStartDate());
        assertEquals(vehicle, createdMaintenance.getVehicle());
        assertEquals(VehicleStatus.MAINTENANCE, createdMaintenance.getVehicle().getVehicleStatus());
        verify(vehicleRepository, times(1)).findById(vehicleId);
        verify(vehicleRepository, times(1)).save(any(Vehicle.class));
        verify(maintenanceRepository, times(1)).save(any(Maintenance.class));
    }

    @Test
    void testDelete() {
        Long maintenanceId = 1L;

        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleStatus(VehicleStatus.MAINTENANCE);

        Maintenance maintenance = new Maintenance();
        maintenance.setVehicle(vehicle);

        when(maintenanceRepository.getReferenceById(maintenanceId)).thenReturn(maintenance);
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);
        when(maintenanceRepository.save(any(Maintenance.class))).thenReturn(maintenance);

        maintenanceService.delete(maintenanceId);

        assertEquals(VehicleStatus.AVAILABLE, vehicle.getVehicleStatus());
        assertEquals(LocalDate.now(), maintenance.getEndDate());
        verify(maintenanceRepository, times(1)).getReferenceById(maintenanceId);
        verify(vehicleRepository, times(1)).save(any(Vehicle.class));
        verify(maintenanceRepository, times(1)).save(any(Maintenance.class));
    }

}
