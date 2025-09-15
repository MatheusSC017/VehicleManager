package com.matheus.VehicleManager.service;

import com.matheus.VehicleManager.dto.SaleRequestDTO;
import com.matheus.VehicleManager.enums.SalesStatus;
import com.matheus.VehicleManager.enums.VehicleStatus;
import com.matheus.VehicleManager.exception.InvalidRequestException;
import com.matheus.VehicleManager.model.Client;
import com.matheus.VehicleManager.model.Sale;
import com.matheus.VehicleManager.model.Vehicle;
import com.matheus.VehicleManager.repository.ClientRepository;
import com.matheus.VehicleManager.repository.SaleRepository;
import com.matheus.VehicleManager.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class SaleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private SaleRepository saleRepository;

    @InjectMocks
    private SaleService saleService;

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

    private Sale buildSale(Long saleId, Vehicle vehicle, Client client, SalesStatus status) {
        Sale sale = new Sale();
        sale.setId(saleId);
        sale.setVehicle(vehicle);
        sale.setClient(client);
        sale.setStatus(status);
        if (status.equals(SalesStatus.RESERVED)) {
            sale.setReserveDate(LocalDate.now());
        }
        sale.setSalesDate(LocalDate.now());
        return sale;
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should return all sales registers with pagination")
    void testFindAll() {
        Sale sale1 = buildSale(1L, new Vehicle(), new Client(), SalesStatus.RESERVED);
        Sale sale2 = buildSale(2L, new Vehicle(), new Client(), SalesStatus.SOLD);
        List<Sale> sales = List.of(sale1, sale2);

        Pageable paging = PageRequest.of(0, 20);
        Page<Sale> salesPage = new PageImpl<>(sales, paging, sales.size());
        when(saleRepository.findAll(any(Pageable.class))).thenReturn(salesPage);

        Page<Sale> foundSales = saleService.findAll(0, 20);

        assertEquals(2, foundSales.getContent().size());
        assertEquals(sale1, foundSales.getContent().get(0));
        assertEquals(sale2, foundSales.getContent().get(1));
        verify(saleRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Should return all sales registers based on a vehicle id")
    void testFindAllByVehicleId() {
        Vehicle vehicle = buildVehicle(1L, VehicleStatus.SOLD);
        Sale sale1 = buildSale(1L, vehicle, new Client(), SalesStatus.CANCELED);
        Sale sale2 = buildSale(2L, vehicle, new Client(), SalesStatus.SOLD);
        List<Sale> sales = List.of(sale1, sale2);

        when(saleRepository.findByVehicleIdOrderByIdDesc(1L)).thenReturn(sales);
        List<Sale> foundSales = saleService.findAllByVehicleId(1L);

        assertEquals(2, foundSales.size());
        assertEquals(sale1, foundSales.get(0));
        assertEquals(1L, foundSales.get(0).getVehicle().getId());
        assertEquals(sale2, foundSales.get(1));
        assertEquals(1L, foundSales.get(1).getVehicle().getId());
        verify(saleRepository, times(1)).findByVehicleIdOrderByIdDesc(1L);
    }

    @Test
    @DisplayName("Should return a specific sale based on id")
    void testFindById() {
        Sale sale = buildSale(1L, new Vehicle(), new Client(), SalesStatus.SOLD);

        when(saleRepository.findById(1L)).thenReturn(Optional.of(sale));
        Sale foundSale = saleService.findById(1L);

        assertEquals(sale, foundSale);
        verify(saleRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should create a new sale and update the vehicle status")
    void testCreate() {
        Client client = buildClient(1L);
        Vehicle vehicle = buildVehicle(1L, VehicleStatus.AVAILABLE);
        Sale sale = buildSale(1L, vehicle, client, SalesStatus.SOLD);

        SaleRequestDTO saleRequestDTO = new SaleRequestDTO();
        saleRequestDTO.setClient(1L);
        saleRequestDTO.setVehicle(1L);
        saleRequestDTO.setStatus(SalesStatus.SOLD);

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);
        when(saleRepository.save(any(Sale.class))).thenReturn(sale);

        Sale createdSale = saleService.create(saleRequestDTO);

        ArgumentCaptor<Vehicle> vehicleCaptor = ArgumentCaptor.forClass(Vehicle.class);
        verify(vehicleRepository).save(vehicleCaptor.capture());
        assertEquals(VehicleStatus.SOLD, vehicleCaptor.getValue().getVehicleStatus());
        assertEquals(vehicle, createdSale.getVehicle());
        assertEquals(client, createdSale.getClient());
        assertEquals(SalesStatus.SOLD, createdSale.getStatus());
        assertEquals(LocalDate.now(), createdSale.getSalesDate());
        verify(clientRepository, times(1)).findById(1L);
        verify(vehicleRepository, times(1)).findById(1L);
        verify(vehicleRepository, times(1)).save(any(Vehicle.class));
        verify(saleRepository, times(1)).save(any(Sale.class));
    }

    @Test
    @DisplayName("Should throw an error if the client requested to the sale creation doesn't exist")
    void testCreateClientNotFound() {
        Vehicle vehicle = buildVehicle(1L, VehicleStatus.AVAILABLE);

        SaleRequestDTO saleRequestDTO = new SaleRequestDTO();
        saleRequestDTO.setClient(1L);
        saleRequestDTO.setVehicle(1L);
        saleRequestDTO.setStatus(SalesStatus.SOLD);

        when(clientRepository.findById(1L)).thenReturn(Optional.empty());
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));;

        assertThrows(InvalidRequestException.class, () -> saleService.create(saleRequestDTO));
    }

    @Test
    @DisplayName("Should throw an error if the vehicle requested to the sale creation doesn't exist")
    void testCreateVehicleNotFound() {
        Client client = buildClient(1L);

        SaleRequestDTO saleRequestDTO = new SaleRequestDTO();
        saleRequestDTO.setClient(1L);
        saleRequestDTO.setVehicle(1L);
        saleRequestDTO.setStatus(SalesStatus.SOLD);

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.empty());;

        assertThrows(InvalidRequestException.class, () -> saleService.create(saleRequestDTO));
    }

    @Test
    @DisplayName("Should throw an error if the vehicle requested to the sale creation it doesn't available")
    void testCreateVehicleNotAvailable() {
        Client client = buildClient(1L);
        Vehicle vehicle = buildVehicle(1L, VehicleStatus.SOLD);

        SaleRequestDTO saleRequestDTO = new SaleRequestDTO();
        saleRequestDTO.setClient(1L);
        saleRequestDTO.setVehicle(1L);
        saleRequestDTO.setStatus(SalesStatus.SOLD);

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));;

        assertThrows(InvalidRequestException.class, () -> saleService.create(saleRequestDTO));
    }

    @Test
    void testUpdateVehicleChange() {
        Client client = buildClient(1L);
        Vehicle oldVehicle = buildVehicle(1L, VehicleStatus.SOLD);
        Vehicle newVehicle = buildVehicle(2L, VehicleStatus.AVAILABLE);
        Sale sale = buildSale(1L, oldVehicle, client, SalesStatus.SOLD);

        SaleRequestDTO saleRequestDTO = new SaleRequestDTO();
        saleRequestDTO.setClient(1L);
        saleRequestDTO.setVehicle(2L);
        saleRequestDTO.setStatus(SalesStatus.SOLD);

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(vehicleRepository.findById(2L)).thenReturn(Optional.of(newVehicle));
        when(saleRepository.findById(1L)).thenReturn(Optional.of(sale));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(newVehicle);
        when(saleRepository.save(any(Sale.class))).thenReturn(sale);

        Sale updatedSale = saleService.update(1L, saleRequestDTO);

        assertEquals(newVehicle, updatedSale.getVehicle());
        assertEquals(client, updatedSale.getClient());
        assertEquals(SalesStatus.SOLD, updatedSale.getStatus());
        assertEquals(sale, updatedSale);
        assertEquals(VehicleStatus.AVAILABLE, oldVehicle.getVehicleStatus());
        assertEquals(VehicleStatus.SOLD, newVehicle.getVehicleStatus());
        verify(clientRepository, times(1)).findById(1L);
        verify(vehicleRepository, times(1)).findById(2L);
        verify(saleRepository, times(1)).findById(1L);
        verify(vehicleRepository, times(2)).save(any(Vehicle.class));
        verify(saleRepository, times(1)).save(any(Sale.class));
    }

    @ParameterizedTest
    @DisplayName("Should update a specific sale and update the vehicle status")
    @CsvSource({
            "RESERVED, SOLD",
            "RESERVED, CANCELED",
            "SOLD, CANCELED",
    })
    void testUpdate(SalesStatus oldStatus, SalesStatus newStatus) {
        Client client = buildClient(1L);
        Vehicle vehicle = buildVehicle(1L, oldStatus.equals(SalesStatus.SOLD) ? VehicleStatus.SOLD : VehicleStatus.RESERVED);
        Sale sale = buildSale(1L, vehicle, client, oldStatus);

        SaleRequestDTO saleRequestDTO = new SaleRequestDTO();
        saleRequestDTO.setClient(1L);
        saleRequestDTO.setVehicle(1L);
        saleRequestDTO.setStatus(newStatus);

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(saleRepository.findById(1L)).thenReturn(Optional.of(sale));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);
        when(saleRepository.save(any(Sale.class))).thenReturn(sale);

        Sale updatedSale = saleService.update(1L, saleRequestDTO);

        ArgumentCaptor<Vehicle> vehicleCaptor = ArgumentCaptor.forClass(Vehicle.class);
        verify(vehicleRepository).save(vehicleCaptor.capture());
        assertEquals(newStatus.equals(SalesStatus.SOLD) ? VehicleStatus.SOLD : VehicleStatus.AVAILABLE, vehicleCaptor.getValue().getVehicleStatus());
        assertEquals(vehicle, updatedSale.getVehicle());
        assertEquals(client, updatedSale.getClient());
        assertEquals(newStatus, updatedSale.getStatus());
        assertEquals(sale, updatedSale);
        verify(clientRepository, times(1)).findById(1L);
        verify(vehicleRepository, times(1)).findById(1L);
        verify(saleRepository, times(1)).findById(1L);
        verify(vehicleRepository, times(1)).save(any(Vehicle.class));
        verify(saleRepository, times(1)).save(any(Sale.class));
    }

    @ParameterizedTest
    @DisplayName("Should throw an error if the transition of status is invalid")
    @CsvSource({
            "SOLD, RESERVED",
            "CANCELED, RESERVED",
            "CANCELED, SOLD",
    })
    void testUpdateInvalidStatusTransition(SalesStatus oldStatus, SalesStatus newStatus) {
        Client client = buildClient(1L);
        Vehicle vehicle = buildVehicle(1L, oldStatus.equals(SalesStatus.SOLD) ? VehicleStatus.SOLD : VehicleStatus.RESERVED);
        Sale sale = buildSale(1L, vehicle, client, oldStatus);

        SaleRequestDTO saleRequestDTO = new SaleRequestDTO();
        saleRequestDTO.setClient(1L);
        saleRequestDTO.setVehicle(1L);
        saleRequestDTO.setStatus(newStatus);

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(saleRepository.findById(1L)).thenReturn(Optional.of(sale));

        assertThrows(InvalidRequestException.class, () -> saleService.update(1L, saleRequestDTO));

        verify(clientRepository, times(1)).findById(1L);
        verify(vehicleRepository, times(1)).findById(1L);
        verify(saleRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw an error if the client requested to the sale update doesn't exist")
    void testUpdateClientNotFound() {
        Client client = buildClient(1L);
        Vehicle vehicle = buildVehicle(1L, VehicleStatus.AVAILABLE);
        Sale sale = buildSale(1L, vehicle, client, SalesStatus.RESERVED);

        SaleRequestDTO saleRequestDTO = new SaleRequestDTO();
        saleRequestDTO.setClient(1L);
        saleRequestDTO.setVehicle(1L);
        saleRequestDTO.setStatus(SalesStatus.SOLD);

        when(clientRepository.findById(1L)).thenReturn(Optional.empty());
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));;
        when(saleRepository.findById(1L)).thenReturn(Optional.of(sale));

        assertThrows(InvalidRequestException.class, () -> saleService.update(1L, saleRequestDTO));
    }

    @Test
    @DisplayName("Should throw an error if the vehicle requested to the sale update doesn't exist")
    void testUpdateVehicleNotFound() {
        Client client = buildClient(1L);
        Vehicle vehicle = buildVehicle(1L, VehicleStatus.AVAILABLE);
        Sale sale = buildSale(1L, vehicle, client, SalesStatus.RESERVED);

        SaleRequestDTO saleRequestDTO = new SaleRequestDTO();
        saleRequestDTO.setClient(1L);
        saleRequestDTO.setVehicle(1L);
        saleRequestDTO.setStatus(SalesStatus.SOLD);

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.empty());;
        when(saleRepository.findById(1L)).thenReturn(Optional.of(sale));

        assertThrows(InvalidRequestException.class, () -> saleService.update(1L, saleRequestDTO));
    }

    @Test
    @DisplayName("Should throw an error if the sale requested to be updated doesn't exist")
    void testUpdateSaleNotFound() {
        Client client = buildClient(1L);
        Vehicle vehicle = buildVehicle(1L, VehicleStatus.AVAILABLE);

        SaleRequestDTO saleRequestDTO = new SaleRequestDTO();
        saleRequestDTO.setClient(1L);
        saleRequestDTO.setVehicle(1L);
        saleRequestDTO.setStatus(SalesStatus.SOLD);

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(saleRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(InvalidRequestException.class, () -> saleService.update(1L, saleRequestDTO));
    }

    @Test
    @DisplayName("Should throw an error if the vehicle requested to the sale update it doesn't available")
    void testUpdateVehicleNotAvailable() {
        Client client = buildClient(1L);
        Vehicle oldVehicle = buildVehicle(1L, VehicleStatus.SOLD);
        Vehicle newVehicle = buildVehicle(2L, VehicleStatus.SOLD);
        Sale sale = buildSale(1L, oldVehicle, client, SalesStatus.RESERVED);

        SaleRequestDTO saleRequestDTO = new SaleRequestDTO();
        saleRequestDTO.setClient(1L);
        saleRequestDTO.setVehicle(1L);
        saleRequestDTO.setStatus(SalesStatus.SOLD);

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(vehicleRepository.findById(2L)).thenReturn(Optional.of(newVehicle));;
        when(saleRepository.findById(1L)).thenReturn(Optional.of(sale));

        assertThrows(InvalidRequestException.class, () -> saleService.update(1L, saleRequestDTO));
    }

}
