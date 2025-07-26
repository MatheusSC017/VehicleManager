package com.matheus.VehicleManager.service;

import com.matheus.VehicleManager.dto.SaleRequestDTO;
import com.matheus.VehicleManager.dto.VehicleMinimalDTO;
import com.matheus.VehicleManager.enums.SalesStatus;
import com.matheus.VehicleManager.enums.VehicleStatus;
import com.matheus.VehicleManager.model.Client;
import com.matheus.VehicleManager.model.Maintenance;
import com.matheus.VehicleManager.model.Sale;
import com.matheus.VehicleManager.model.Vehicle;
import com.matheus.VehicleManager.repository.ClientRepository;
import com.matheus.VehicleManager.repository.MaintenanceRepository;
import com.matheus.VehicleManager.repository.SaleRepository;
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


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindAll() {
        Sale sale1 = new Sale();
        sale1.setId(1L);
        sale1.setVehicle(new Vehicle());
        sale1.setClient(new Client());
        sale1.setStatus(SalesStatus.RESERVED);

        Sale sale2 = new Sale();
        sale2.setId(2L);
        sale2.setVehicle(new Vehicle());
        sale2.setClient(new Client());
        sale2.setStatus(SalesStatus.SOLD);

        List<Sale> sales = new ArrayList<>();
        sales.add(sale1);
        sales.add(sale2);

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
    void testFindAllByVehicleId() {
        Long vehicleId = 1L;
        Vehicle vehicle = new Vehicle();
        vehicle.setId(vehicleId);

        Sale sale1 = new Sale();
        sale1.setId(1L);
        sale1.setVehicle(vehicle);
        sale1.setClient(new Client());
        sale1.setStatus(SalesStatus.CANCELED);

        Sale sale2 = new Sale();
        sale2.setId(2L);
        sale2.setVehicle(vehicle);
        sale2.setClient(new Client());
        sale2.setStatus(SalesStatus.SOLD);

        List<Sale> sales = new ArrayList<>();
        sales.add(sale1);
        sales.add(sale2);

        when(saleRepository.findByVehicleIdOrderByIdDesc(vehicleId)).thenReturn(sales);

        List<Sale> foundSales = saleService.findAllByVehicleId(vehicleId);

        assertEquals(2, foundSales.size());
        assertEquals(sale1, foundSales.get(0));
        assertEquals(vehicleId, foundSales.get(0).getVehicle().getId());
        assertEquals(sale2, foundSales.get(1));
        assertEquals(vehicleId, foundSales.get(1).getVehicle().getId());
        verify(saleRepository, times(1)).findByVehicleIdOrderByIdDesc(vehicleId);
    }

    @Test
    void testFindById() {
        Long saleId = 1L;

        Sale sale = new Sale();
        sale.setId(saleId);
        sale.setVehicle(new Vehicle());
        sale.setClient(new Client());
        sale.setStatus(SalesStatus.SOLD);

        when(saleRepository.getReferenceById(saleId)).thenReturn(sale);

        Sale foundSale = saleService.findById(saleId);

        assertEquals(sale, foundSale);
        verify(saleRepository, times(1)).getReferenceById(saleId);
    }

    @Test
    void testCreate() {
        VehicleMinimalDTO vehicleMinimalDTO = new VehicleMinimalDTO(
            1L, "TestVehicleChassi", "TestBrand", "TestModel"
        );

        Client client = new Client();
        client.setId(1L);

        SaleRequestDTO saleRequestDTO = new SaleRequestDTO();
        saleRequestDTO.setClient(client);
        saleRequestDTO.setVehicle(vehicleMinimalDTO);
        saleRequestDTO.setStatus(SalesStatus.SOLD);

        Vehicle vehicle = new Vehicle();
        vehicle.setId(1L);
        vehicle.setChassi("TestVehicleChassi");
        vehicle.setBrand("TestBrand");
        vehicle.setModel("TestModel");
        vehicle.setVehicleStatus(VehicleStatus.AVAILABLE);

        Sale sale = new Sale();
        sale.setId(1L);
        sale.setVehicle(vehicle);
        sale.setClient(client);
        sale.setStatus(SalesStatus.SOLD);
        sale.setSalesDate(LocalDate.now());

        when(clientRepository.findById(client.getId())).thenReturn(Optional.of(client));
        when(vehicleRepository.findById(vehicleMinimalDTO.id())).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);
        when(saleRepository.save(any(Sale.class))).thenReturn(sale);

        Sale createdSale = saleService.create(saleRequestDTO);

        assertEquals(vehicle, createdSale.getVehicle());
        assertEquals(client, createdSale.getClient());
        assertEquals(SalesStatus.SOLD, createdSale.getStatus());
        assertEquals(LocalDate.now(), createdSale.getSalesDate());
        assertEquals(VehicleStatus.SOLD, createdSale.getVehicle().getVehicleStatus());
        verify(clientRepository, times(1)).findById(client.getId());
        verify(vehicleRepository, times(1)).findById(vehicleMinimalDTO.id());
        verify(vehicleRepository, times(1)).save(any(Vehicle.class));
        verify(saleRepository, times(1)).save(any(Sale.class));
    }

    @Test
    void testUpdate() {
        Long saleId = 1L;

        VehicleMinimalDTO vehicleMinimalDTO = new VehicleMinimalDTO(
                1L, "TestVehicleChassi", "TestBrand", "TestModel"
        );

        Client client = new Client();
        client.setId(1L);

        SaleRequestDTO saleRequestDTO = new SaleRequestDTO();
        saleRequestDTO.setClient(client);
        saleRequestDTO.setVehicle(vehicleMinimalDTO);
        saleRequestDTO.setStatus(SalesStatus.SOLD);

        Vehicle vehicle = new Vehicle();
        vehicle.setId(1L);
        vehicle.setChassi("TestVehicleChassi");
        vehicle.setBrand("TestBrand");
        vehicle.setModel("TestModel");
        vehicle.setVehicleStatus(VehicleStatus.AVAILABLE);

        Sale sale = new Sale();
        sale.setId(saleId);
        sale.setVehicle(vehicle);
        sale.setClient(client);
        sale.setStatus(SalesStatus.RESERVED);
        sale.setReserveDate(LocalDate.now());
        sale.setSalesDate(LocalDate.now());

        when(clientRepository.findById(client.getId())).thenReturn(Optional.of(client));
        when(vehicleRepository.findById(vehicleMinimalDTO.id())).thenReturn(Optional.of(vehicle));
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);
        when(saleRepository.save(any(Sale.class))).thenReturn(sale);

        Sale updatedSale = saleService.update(saleId, saleRequestDTO);

        assertEquals(vehicle, updatedSale.getVehicle());
        assertEquals(client, updatedSale.getClient());
        assertEquals(SalesStatus.SOLD, updatedSale.getStatus());
        assertEquals(sale, updatedSale);
        assertEquals(LocalDate.now(), updatedSale.getReserveDate());
        assertEquals(LocalDate.now(), updatedSale.getSalesDate());
        assertEquals(VehicleStatus.SOLD, updatedSale.getVehicle().getVehicleStatus());
        verify(clientRepository, times(1)).findById(client.getId());
        verify(vehicleRepository, times(1)).findById(vehicleMinimalDTO.id());
        verify(saleRepository, times(1)).findById(saleId);
        verify(vehicleRepository, times(1)).save(any(Vehicle.class));
        verify(saleRepository, times(1)).save(any(Sale.class));
    }

}
