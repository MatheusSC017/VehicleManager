package com.matheus.VehicleManager.service;

import com.matheus.VehicleManager.dto.FinancingRequestDTO;
import com.matheus.VehicleManager.dto.VehicleMinimalDTO;
import com.matheus.VehicleManager.enums.FinancingStatus;
import com.matheus.VehicleManager.enums.VehicleStatus;
import com.matheus.VehicleManager.exception.InvalidRequestException;
import com.matheus.VehicleManager.model.Client;
import com.matheus.VehicleManager.model.Financing;
import com.matheus.VehicleManager.model.Vehicle;
import com.matheus.VehicleManager.repository.ClientRepository;
import com.matheus.VehicleManager.repository.FinancingRepository;
import com.matheus.VehicleManager.repository.VehicleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
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

import static org.junit.jupiter.api.Assertions.*;
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

    private Client buildClient(Long id) {
        Client client = new Client();
        client.setId(id);
        return client;
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

    private Financing buildFinancing(Long id, Vehicle vehicle, Client client, FinancingStatus status) {
        Financing financing = new Financing();
        financing.setId(id);
        financing.setVehicle(vehicle);
        financing.setClient(client);
        financing.setTotalAmount(new BigDecimal(100000));
        financing.setDownPayment(new BigDecimal(40000));
        financing.setInstallmentCount(40);
        financing.setInstallmentValue(new BigDecimal(1200));
        financing.setAnnualInterestRate(new BigDecimal("1.2"));
        financing.setContractDate(LocalDate.now());
        financing.setFirstInstallmentDate(LocalDate.now().plusMonths(1));
        financing.setStatus(status);
        return financing;
    }

    @Test
    @DisplayName("Should return all financings with pagination")
    void testGetAll() {
        Financing financing1 = buildFinancing(1L, new Vehicle(), new Client(), FinancingStatus.DRAFT);
        Financing financing2 = buildFinancing(2L, new Vehicle(), new Client(), FinancingStatus.ACTIVE);

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
    @DisplayName("Should return a specific financing based on id")
    void testGetById() {
        Long financingId = 1L;
        Financing financing = buildFinancing(financingId, new Vehicle(), new Client(), FinancingStatus.DRAFT);
        when(financingRepository.getReferenceById(financingId)).thenReturn(financing);

        Financing foundFinancing = financingService.getById(financingId);

        assertEquals(financing, foundFinancing);
        verify(financingRepository, times(1)).getReferenceById(financingId);
    }

    @Test
    @DisplayName("Should throw an error if there is no financing with the specific id")
    void testGetByIdNotFound() {
        when(financingRepository.getReferenceById(99L)).thenThrow(EntityNotFoundException.class);
        assertThrows(EntityNotFoundException.class, () -> financingService.getById(99L));
    }

    @Test
    @DisplayName("Must return a specific loan based on the id and the status must be different from Canceled")
    void testGetByVehicleIdNotCanceled() {
        Long vehicleId = 1L;
        Vehicle vehicle = buildVehicle(vehicleId, VehicleStatus.SOLD);
        Financing financing = buildFinancing(1L, vehicle, new Client(), FinancingStatus.DRAFT);

        when(financingRepository.findActiveByVehicleId(vehicleId)).thenReturn(Optional.of(financing));
        Optional<Financing> foundFinancing = financingService.getByVehicleIdNotCanceled(vehicleId);

        assertEquals(financing, foundFinancing.get());
        assertEquals(vehicle, foundFinancing.get().getVehicle());
        verify(financingRepository, times(1)).findActiveByVehicleId(vehicleId);
    }

    @Test
    @DisplayName("Should create a new financing and update the vehicle status")
    void testCreate() {
        Client client = buildClient(1L);
        Vehicle vehicle = buildVehicle(1L, VehicleStatus.AVAILABLE);
        Financing financing = buildFinancing(1L, vehicle, client, FinancingStatus.DRAFT);

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

        when(clientRepository.findById(client.getId())).thenReturn(Optional.of(client));
        when(vehicleRepository.findById(vehicleMinimalDTO.id())).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);
        when(financingRepository.save(any(Financing.class))).thenReturn(financing);

        Financing createdFinancing = financingService.create(financingRequestDTO);

        ArgumentCaptor<Vehicle> vehicleCaptor = ArgumentCaptor.forClass(Vehicle.class);
        verify(vehicleRepository).save(vehicleCaptor.capture());
        assertEquals(VehicleStatus.SOLD, vehicleCaptor.getValue().getVehicleStatus());
        assertEquals(financing, createdFinancing);
        assertEquals(vehicle, createdFinancing.getVehicle());
        verify(clientRepository, times(1)).findById(client.getId());
        verify(vehicleRepository, times(1)).findById(vehicleMinimalDTO.id());
        verify(vehicleRepository, times(1)).save(any(Vehicle.class));
        verify(financingRepository, times(1)).save(any(Financing.class));

    }

    @Test
    @DisplayName("Should throw an error if the client requested to the financing creation doesn't exist")
    void testCreateClientNotFound() {
        Client client = buildClient(1L);
        Vehicle vehicle = buildVehicle(1L, VehicleStatus.AVAILABLE);

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

        when(clientRepository.findById(client.getId())).thenReturn(Optional.empty());
        when(vehicleRepository.findById(vehicleMinimalDTO.id())).thenReturn(Optional.of(vehicle));

        assertThrows(InvalidRequestException.class, () -> financingService.create(financingRequestDTO));

        verify(clientRepository, times(1)).findById(client.getId());
        verify(vehicleRepository, times(1)).findById(vehicleMinimalDTO.id());
    }

    @Test
    @DisplayName("Should throw an error if the vehicle requested to the financing creation doesn't exist")
    void testCreateVehicleNotFound() {
        Client client = buildClient(1L);

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

        when(clientRepository.findById(client.getId())).thenReturn(Optional.of(client));
        when(vehicleRepository.findById(vehicleMinimalDTO.id())).thenReturn(Optional.empty());

        assertThrows(InvalidRequestException.class, () -> financingService.create(financingRequestDTO));

        verify(clientRepository, times(1)).findById(client.getId());
        verify(vehicleRepository, times(1)).findById(vehicleMinimalDTO.id());
    }

    @Test
    @DisplayName("Should throw an error if the vehicle requested to the financing creation doesn't available")
    void testCreateVehicleNotAvailable() {
        Client client = buildClient(1L);
        Vehicle vehicle = buildVehicle(1L, VehicleStatus.SOLD);

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

        when(clientRepository.findById(client.getId())).thenReturn(Optional.of(client));
        when(vehicleRepository.findById(vehicleMinimalDTO.id())).thenReturn(Optional.of(vehicle));

        assertThrows(InvalidRequestException.class, () -> financingService.create(financingRequestDTO));

        verify(clientRepository, times(1)).findById(client.getId());
        verify(vehicleRepository, times(1)).findById(vehicleMinimalDTO.id());
    }

    @Test
    @DisplayName("Should update the data of a specific financing and update the vehicle status if needed")
    void testUpdate() {
        Long financingId = 1L;

        Client client = buildClient(1L);
        Vehicle oldVehicle = buildVehicle(1L, VehicleStatus.SOLD);
        Vehicle newVehicle = buildVehicle(2L, VehicleStatus.AVAILABLE);
        Financing financing = buildFinancing(1L, oldVehicle, client, FinancingStatus.DRAFT);

        VehicleMinimalDTO vehicleMinimalDTO = new VehicleMinimalDTO(
                2L,
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

        when(clientRepository.findById(client.getId())).thenReturn(Optional.of(client));
        when(vehicleRepository.findById(vehicleMinimalDTO.id())).thenReturn(Optional.of(newVehicle));
        when(financingRepository.getReferenceById(financingId)).thenReturn(financing);
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(newVehicle);
        when(financingRepository.save(any(Financing.class))).thenReturn(financing);

        Financing updatedFinancing = financingService.update(financingId, financingRequestDTO);

        assertEquals(financing, updatedFinancing);
        assertEquals(newVehicle, updatedFinancing.getVehicle());
        assertEquals(VehicleStatus.AVAILABLE, oldVehicle.getVehicleStatus());
        assertEquals(VehicleStatus.SOLD, newVehicle.getVehicleStatus());
        verify(clientRepository, times(1)).findById(client.getId());
        verify(vehicleRepository, times(1)).findById(vehicleMinimalDTO.id());
        verify(vehicleRepository, times(2)).save(any(Vehicle.class));
        verify(financingRepository, times(1)).getReferenceById(financingId);
        verify(financingRepository, times(1)).save(any(Financing.class));
    }

    @Test
    @DisplayName("Should throw an error if the client requested to the financing updating doesn't exist")
    void testUpdateClientNotFound() {
        Long financingId = 1L;

        Client client = buildClient(1L);
        Vehicle vehicle = buildVehicle(1L, VehicleStatus.SOLD);
        Financing financing = buildFinancing(1L, vehicle, client, FinancingStatus.DRAFT);

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

        when(clientRepository.findById(client.getId())).thenReturn(Optional.empty());
        when(vehicleRepository.findById(vehicleMinimalDTO.id())).thenReturn(Optional.of(vehicle));
        when(financingRepository.getReferenceById(financingId)).thenReturn(financing);

        assertThrows(InvalidRequestException.class, () -> financingService.update(financingId, financingRequestDTO));

        verify(clientRepository, times(1)).findById(client.getId());
        verify(vehicleRepository, times(1)).findById(vehicleMinimalDTO.id());
        verify(financingRepository, times(1)).getReferenceById(financingId);
    }

    @Test
    @DisplayName("Should throw an error if the vehicle requested to the financing updating doesn't exist")
    void testUpdateVehicleNotFound() {
        Long financingId = 1L;

        Client client = buildClient(1L);
        Vehicle vehicle = buildVehicle(1L, VehicleStatus.SOLD);
        Financing financing = buildFinancing(1L, vehicle, client, FinancingStatus.DRAFT);

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

        when(clientRepository.findById(client.getId())).thenReturn(Optional.of(client));
        when(vehicleRepository.findById(vehicleMinimalDTO.id())).thenReturn(Optional.empty());
        when(financingRepository.getReferenceById(financingId)).thenReturn(financing);

        assertThrows(InvalidRequestException.class, () -> financingService.update(financingId, financingRequestDTO));

        verify(clientRepository, times(1)).findById(client.getId());
        verify(vehicleRepository, times(1)).findById(vehicleMinimalDTO.id());
        verify(financingRepository, times(1)).getReferenceById(financingId);
    }

    @Test
    @DisplayName("Should throw an error if the new vehicle requested to the financing updating doesn't available")
    void testUpdateVehicleNotAvailable() {
        Long financingId = 1L;

        Client client = buildClient(1L);
        Vehicle oldVehicle = buildVehicle(1L, VehicleStatus.SOLD);
        Vehicle newVehicle = buildVehicle(2L, VehicleStatus.SOLD);
        Financing financing = buildFinancing(1L, oldVehicle, client, FinancingStatus.DRAFT);

        VehicleMinimalDTO vehicleMinimalDTO = new VehicleMinimalDTO(
                2L,
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

        when(clientRepository.findById(client.getId())).thenReturn(Optional.of(client));
        when(vehicleRepository.findById(vehicleMinimalDTO.id())).thenReturn(Optional.of(newVehicle));
        when(financingRepository.getReferenceById(financingId)).thenReturn(financing);

        assertThrows(InvalidRequestException.class, () -> financingService.update(financingId, financingRequestDTO));

        verify(clientRepository, times(1)).findById(client.getId());
        verify(vehicleRepository, times(1)).findById(vehicleMinimalDTO.id());
        verify(financingRepository, times(1)).getReferenceById(financingId);
    }

    @ParameterizedTest
    @CsvSource({
        "DRAFT, DEFAULTED",
        "ACTIVE, DRAFT",
        "DEFAULTED, DRAFT",
        "DEFAULTED, ACTIVE",
        "DEFAULTED, CANCELED",
        "CANCELED, DRAFT",
        "CANCELED, DEFAULTED",
        "CANCELED, ACTIVE"
    })
    @DisplayName("Should throw an error to all invalid transition of status")
    void testUpdateStatusForInvalidTransitions(FinancingStatus oldStatus, FinancingStatus newStatus) {
        Long financingId = 1L;
        Vehicle vehicle = buildVehicle(1L, VehicleStatus.SOLD);
        Financing financing = buildFinancing(1L, vehicle, new Client(), oldStatus);

        when(financingRepository.findById(financingId)).thenReturn(Optional.of(financing));

        assertThrows(InvalidRequestException.class, () -> financingService.updateStatus(financingId, newStatus));

        verify(financingRepository, times(1)).findById(financingId);
    }

    @ParameterizedTest
    @CsvSource({
            "DRAFT, ACTIVE",
            "DRAFT, CANCELED",
            "ACTIVE, DEFAULTED",
            "ACTIVE, CANCELED"
    })
    @DisplayName("Should update the financing status")
    void testUpdateStatusForValidTransitions(FinancingStatus oldStatus, FinancingStatus newStatus) {
        Vehicle vehicle = buildVehicle(1L, VehicleStatus.SOLD);
        Financing financing = buildFinancing(1L, vehicle, buildClient(1L), oldStatus);
        when(financingRepository.findById(1L)).thenReturn(Optional.of(financing));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);
        when(financingRepository.save(any(Financing.class))).thenReturn(financing);

        financingService.updateStatus(1L, newStatus);

        assertEquals(newStatus, financing.getStatus());
        verify(financingRepository, times(1)).findById(1L);
        verify(financingRepository, times(1)).save(any(Financing.class));

        if (newStatus == FinancingStatus.CANCELED) {
            ArgumentCaptor<Vehicle> vehicleCaptor = ArgumentCaptor.forClass(Vehicle.class);
            verify(vehicleRepository).save(vehicleCaptor.capture());
            assertEquals(VehicleStatus.AVAILABLE, vehicleCaptor.getValue().getVehicleStatus());
            verify(vehicleRepository, times(1)).save(any(Vehicle.class));
        }
    }
}
