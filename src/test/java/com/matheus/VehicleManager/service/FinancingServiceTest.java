package com.matheus.VehicleManager.service;

import com.matheus.VehicleManager.dto.FinancingRequestDTO;
import com.matheus.VehicleManager.dto.VehicleMinimalDTO;
import com.matheus.VehicleManager.enums.FinancingStatus;
import com.matheus.VehicleManager.enums.VehicleStatus;
import com.matheus.VehicleManager.model.Client;
import com.matheus.VehicleManager.model.Financing;
import com.matheus.VehicleManager.model.Vehicle;
import com.matheus.VehicleManager.repository.ClientRepository;
import com.matheus.VehicleManager.repository.FinancingRepository;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class FinancingServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private FinancingRepository financingRepository;

    @InjectMocks
    private FinancingService financingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAll() {
        Financing financing1 = new Financing();
        financing1.setId(1L);
        financing1.setVehicle(new Vehicle());
        financing1.setClient(new Client());
        financing1.setTotalAmount(new BigDecimal(100000));
        financing1.setDownPayment(new BigDecimal(40000));
        financing1.setInstallmentCount(40);
        financing1.setInstallmentValue(new BigDecimal(1200));
        financing1.setAnnualInterestRate(new BigDecimal("1.2"));
        financing1.setContractDate(LocalDate.now());
        financing1.setFirstInstallmentDate(LocalDate.now().plusMonths(1));
        financing1.setStatus(FinancingStatus.DRAFT);

        Financing financing2 = new Financing();
        financing2.setId(2L);
        financing2.setVehicle(new Vehicle());
        financing2.setClient(new Client());
        financing2.setTotalAmount(new BigDecimal(90000));
        financing2.setDownPayment(new BigDecimal(50000));
        financing2.setInstallmentCount(24);
        financing2.setInstallmentValue(new BigDecimal(1400));
        financing2.setAnnualInterestRate(new BigDecimal("1.5"));
        financing2.setContractDate(LocalDate.now().plusDays(15));
        financing2.setFirstInstallmentDate(LocalDate.now().plusMonths(2));
        financing2.setStatus(FinancingStatus.ACTIVE);

        List<Financing> financings = new ArrayList<>();
        financings.add(financing1);
        financings.add(financing2);

        Pageable paging = PageRequest.of(0, 20);
        Page<Financing> financingsPage = new PageImpl<>(financings, paging, financings.size());
        when(financingRepository.findAll(any(Pageable.class))).thenReturn(financingsPage);

        Page<Financing> foundFinancings = financingService.getAll(0, 20);

        assertEquals(2, foundFinancings.getContent().size());
        assertEquals(financing1, foundFinancings.getContent().get(0));
        assertEquals(financing2, foundFinancings.getContent().get(1));
        verify(financingRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void testGetById() {
        Long financingId = 1L;

        Financing financing = new Financing();
        financing.setId(1L);
        financing.setVehicle(new Vehicle());
        financing.setClient(new Client());
        financing.setTotalAmount(new BigDecimal(100000));
        financing.setDownPayment(new BigDecimal(40000));
        financing.setInstallmentCount(40);
        financing.setInstallmentValue(new BigDecimal(1200));
        financing.setAnnualInterestRate(new BigDecimal("1.2"));
        financing.setContractDate(LocalDate.now());
        financing.setFirstInstallmentDate(LocalDate.now().plusMonths(1));
        financing.setStatus(FinancingStatus.DRAFT);

        when(financingRepository.getReferenceById(financingId)).thenReturn(financing);

        Financing foundFinancing = financingService.getById(financingId);

        assertEquals(financing, foundFinancing);
        verify(financingRepository, times(1)).getReferenceById(financingId);
    }

    @Test
    void testGetByVehicleIdNotCanceled() {
        Long vehicleId = 1L;

        Vehicle vehicle = new Vehicle();
        vehicle.setId(vehicleId);

        Financing financing = new Financing();
        financing.setId(1L);
        financing.setVehicle(vehicle);
        financing.setClient(new Client());
        financing.setTotalAmount(new BigDecimal(100000));
        financing.setDownPayment(new BigDecimal(40000));
        financing.setInstallmentCount(40);
        financing.setInstallmentValue(new BigDecimal(1200));
        financing.setAnnualInterestRate(new BigDecimal("1.2"));
        financing.setContractDate(LocalDate.now());
        financing.setFirstInstallmentDate(LocalDate.now().plusMonths(1));
        financing.setStatus(FinancingStatus.DRAFT);

        when(financingRepository.findActiveByVehicleId(vehicleId)).thenReturn(Optional.of(financing));

        Optional<Financing> foundFinancing = financingService.getByVehicleIdNotCanceled(vehicleId);

        assertEquals(financing, foundFinancing.get());
        assertEquals(vehicle, foundFinancing.get().getVehicle());
        verify(financingRepository, times(1)).findActiveByVehicleId(vehicleId);

    }

    @Test
    void testCreate() {
        Client client = new Client();
        client.setId(1L);

        VehicleMinimalDTO vehicleMinimalDTO = new VehicleMinimalDTO(
                1L,
                "TestVehicleChassi",
                "TestVehicleBrand",
                "TestVehicleModel"
        );

        FinancingRequestDTO financingRequestDTO = new FinancingRequestDTO();
        financingRequestDTO.setVehicle(vehicleMinimalDTO);
        financingRequestDTO.setClient(client);
        financingRequestDTO.setTotalAmount(new BigDecimal(100000));
        financingRequestDTO.setDownPayment(new BigDecimal(40000));
        financingRequestDTO.setInstallmentCount(40);
        financingRequestDTO.setInstallmentValue(new BigDecimal(1200));
        financingRequestDTO.setAnnualInterestRate(new BigDecimal("1.2"));
        financingRequestDTO.setContractDate(LocalDate.now());
        financingRequestDTO.setFirstInstallmentDate(LocalDate.now().plusMonths(1));

        Vehicle vehicle = new Vehicle();
        vehicle.setId(1L);
        vehicle.setChassi("TestVehicleChassi");
        vehicle.setBrand("TestVehicleBrand");
        vehicle.setModel("TestVehicleModel");
        vehicle.setVehicleStatus(VehicleStatus.AVAILABLE);

        Financing financing = new Financing();
        financing.setId(1L);
        financing.setVehicle(vehicle);
        financing.setClient(client);
        financing.setTotalAmount(new BigDecimal(100000));
        financing.setDownPayment(new BigDecimal(40000));
        financing.setInstallmentCount(40);
        financing.setInstallmentValue(new BigDecimal(1200));
        financing.setAnnualInterestRate(new BigDecimal("1.2"));
        financing.setContractDate(LocalDate.now());
        financing.setFirstInstallmentDate(LocalDate.now().plusMonths(1));
        financing.setStatus(FinancingStatus.DRAFT);

        when(clientRepository.findById(client.getId())).thenReturn(Optional.of(client));
        when(vehicleRepository.findById(vehicleMinimalDTO.id())).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);
        when(financingRepository.save(any(Financing.class))).thenReturn(financing);

        Financing createdFinancing = financingService.create(financingRequestDTO);

        assertEquals(financing, createdFinancing);
        assertEquals(vehicle, createdFinancing.getVehicle());
        assertEquals(VehicleStatus.SOLD, createdFinancing.getVehicle().getVehicleStatus());
        verify(clientRepository, times(1)).findById(client.getId());
        verify(vehicleRepository, times(1)).findById(vehicleMinimalDTO.id());
        verify(vehicleRepository, times(1)).save(any(Vehicle.class));
        verify(financingRepository, times(1)).save(any(Financing.class));

    }

    @Test
    void testUpdate() {
        Long financingId = 1L;

        Client client = new Client();
        client.setId(1L);

        VehicleMinimalDTO vehicleMinimalDTO = new VehicleMinimalDTO(
                1L,
                "TestVehicleChassi",
                "TestVehicleBrand",
                "TestVehicleModel"
        );

        FinancingRequestDTO financingRequestDTO = new FinancingRequestDTO();
        financingRequestDTO.setVehicle(vehicleMinimalDTO);
        financingRequestDTO.setClient(client);
        financingRequestDTO.setTotalAmount(new BigDecimal(90000));
        financingRequestDTO.setDownPayment(new BigDecimal(50000));
        financingRequestDTO.setInstallmentCount(24);
        financingRequestDTO.setInstallmentValue(new BigDecimal(1500));
        financingRequestDTO.setAnnualInterestRate(new BigDecimal("1.5"));
        financingRequestDTO.setContractDate(LocalDate.now().plusDays(15));
        financingRequestDTO.setFirstInstallmentDate(LocalDate.now().plusMonths(2));

        Vehicle vehicle = new Vehicle();
        vehicle.setId(1L);
        vehicle.setChassi("TestVehicleChassi");
        vehicle.setBrand("TestVehicleBrand");
        vehicle.setModel("TestVehicleModel");
        vehicle.setVehicleStatus(VehicleStatus.AVAILABLE);

        Financing financing = new Financing();
        financing.setId(financingId);
        financing.setVehicle(vehicle);
        financing.setClient(client);
        financing.setTotalAmount(new BigDecimal(100000));
        financing.setDownPayment(new BigDecimal(40000));
        financing.setInstallmentCount(40);
        financing.setInstallmentValue(new BigDecimal(1200));
        financing.setAnnualInterestRate(new BigDecimal("1.2"));
        financing.setContractDate(LocalDate.now());
        financing.setFirstInstallmentDate(LocalDate.now().plusMonths(1));
        financing.setStatus(FinancingStatus.DRAFT);

        when(clientRepository.findById(client.getId())).thenReturn(Optional.of(client));
        when(vehicleRepository.findById(vehicleMinimalDTO.id())).thenReturn(Optional.of(vehicle));
        when(financingRepository.getReferenceById(vehicleMinimalDTO.id())).thenReturn(financing);
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);
        when(financingRepository.save(any(Financing.class))).thenReturn(financing);

        Financing updatedFinancing = financingService.update(financingId, financingRequestDTO);

        assertEquals(financing, updatedFinancing);
        assertEquals(financingId, updatedFinancing.getId());
        assertEquals(vehicle, updatedFinancing.getVehicle());
        assertEquals(client, updatedFinancing.getClient());
        assertEquals(FinancingStatus.DRAFT, updatedFinancing.getStatus());
        assertEquals(new BigDecimal(90000), updatedFinancing.getTotalAmount());
        assertEquals(new BigDecimal(50000), updatedFinancing.getDownPayment());
        assertEquals(24, updatedFinancing.getInstallmentCount());
        assertEquals(new BigDecimal(1500), updatedFinancing.getInstallmentValue());
        assertEquals(new BigDecimal("1.5"), updatedFinancing.getAnnualInterestRate());
        assertEquals(LocalDate.now().plusDays(15), updatedFinancing.getContractDate());
        assertEquals(LocalDate.now().plusMonths(2), updatedFinancing.getFirstInstallmentDate());
        verify(clientRepository, times(1)).findById(client.getId());
        verify(vehicleRepository, times(1)).findById(vehicleMinimalDTO.id());
        verify(vehicleRepository, times(1)).save(any(Vehicle.class));
        verify(financingRepository, times(1)).getReferenceById(vehicleMinimalDTO.id());
        verify(financingRepository, times(1)).save(any(Financing.class));

    }

    @Test
    void  testUpdateStatus() {
        Long financingId = 1L;

        Vehicle vehicle = new Vehicle();
        vehicle.setId(1L);
        vehicle.setChassi("TestVehicleChassi");
        vehicle.setBrand("TestVehicleBrand");
        vehicle.setModel("TestVehicleModel");
        vehicle.setVehicleStatus(VehicleStatus.SOLD);

        Financing financing = new Financing();
        financing.setId(1L);
        financing.setVehicle(vehicle);
        financing.setClient(new Client());
        financing.setTotalAmount(new BigDecimal(100000));
        financing.setDownPayment(new BigDecimal(40000));
        financing.setInstallmentCount(40);
        financing.setInstallmentValue(new BigDecimal(1200));
        financing.setAnnualInterestRate(new BigDecimal("1.2"));
        financing.setContractDate(LocalDate.now());
        financing.setFirstInstallmentDate(LocalDate.now().plusMonths(1));
        financing.setStatus(FinancingStatus.ACTIVE);

        when(financingRepository.findById(financingId)).thenReturn(Optional.of(financing));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);
        when(financingRepository.save(any(Financing.class))).thenReturn(financing);

        financingService.updateStatus(financingId, FinancingStatus.CANCELED);

        assertEquals(FinancingStatus.CANCELED, financing.getStatus());
        assertEquals(VehicleStatus.AVAILABLE, vehicle.getVehicleStatus());
        verify(financingRepository, times(1)).findById(financingId);
        verify(vehicleRepository, times(1)).save(any(Vehicle.class));
        verify(financingRepository, times(1)).save(any(Financing.class));


    }

}
