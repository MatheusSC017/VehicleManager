package com.matheus.VehicleManager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matheus.VehicleManager.dto.SaleRequestDTO;
import com.matheus.VehicleManager.dto.VehicleMinimalDTO;
import com.matheus.VehicleManager.enums.SalesStatus;
import com.matheus.VehicleManager.enums.VehicleFuel;
import com.matheus.VehicleManager.enums.VehicleStatus;
import com.matheus.VehicleManager.enums.VehicleType;
import com.matheus.VehicleManager.model.Client;
import com.matheus.VehicleManager.model.Sale;
import com.matheus.VehicleManager.model.Vehicle;
import com.matheus.VehicleManager.security.JwtAuthenticationFilter;
import com.matheus.VehicleManager.security.JwtUtil;
import com.matheus.VehicleManager.service.SaleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SalesController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class SaleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SaleService saleService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private Client buildClient(Long id) {
        Client client = new Client();
        client.setId(id);
        client.setFirstName("TestFirstName");
        client.setLastName("TestLastName");
        client.setEmail("test@test.com");
        client.setPhone("19 88888-8888");
        return client;
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

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Should return all sales with pagination")
    void testGetAll() throws Exception {
        Sale sale1 = buildSale(1L, new Vehicle(), new Client(), SalesStatus.SOLD);
        Sale sale2 = buildSale(2L, new Vehicle(), new Client(), SalesStatus.RESERVED);
        when(saleService.findAll(0, 10))
                .thenReturn(new PageImpl<>(List.of(sale1, sale2), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/sales")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].status").value("SOLD"))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].status").value("RESERVED"));
    }

    @Test
    @DisplayName("Should return all sales based on vehicle id")
    void testGetAllByVehicle() throws Exception {
        Vehicle vehicle = buildVehicle(1L, VehicleStatus.SOLD);

        Sale sale1 = buildSale(1L, vehicle, new Client(), SalesStatus.CANCELED);
        Sale sale2 = buildSale(2L, vehicle, new Client(), SalesStatus.SOLD);
        when(saleService.findAllByVehicleId(1L)).thenReturn(List.of(sale1, sale2));

        mockMvc.perform(get("/api/sales/vehicle/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("CANCELED"))
                .andExpect(jsonPath("$[0].vehicle.id").value(1))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].vehicle.id").value(1))
                .andExpect(jsonPath("$[1].status").value("SOLD"));
    }

    @Test
    @DisplayName("Should return a specific sale based on id")
    void testGet() throws Exception {
        Sale sale = buildSale(1L, new Vehicle(), new Client(), SalesStatus.SOLD);
        when(saleService.findById(1L)).thenReturn(sale);

        mockMvc.perform(get("/api/sales/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("SOLD"));
    }

    @Test
    @DisplayName("Should create a new sale")
    void testCreateSuccess() throws Exception {
        Client client = buildClient(1L);
        Vehicle vehicle = buildVehicle(1L, VehicleStatus.SOLD);
        Sale sale = buildSale(1L, vehicle, client, SalesStatus.SOLD);

        VehicleMinimalDTO vehicleMinimalDTO = new VehicleMinimalDTO(1L, "TestChassi", "TestBrand", "TestModel");
        SaleRequestDTO saleRequestDTO = new SaleRequestDTO();
        saleRequestDTO.setClient(client);
        saleRequestDTO.setVehicle(vehicleMinimalDTO);
        saleRequestDTO.setStatus(SalesStatus.SOLD);

        when(saleService.create(any(SaleRequestDTO.class))).thenReturn(sale);

        mockMvc.perform(post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(saleRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.vehicle.id").value(1L))
                .andExpect(jsonPath("$.client.id").value(1L))
                .andExpect(jsonPath("$.status").value("SOLD"));

        verify(saleService, times(1)).create(any(SaleRequestDTO.class));
    }

    @Test
    @DisplayName("Should throw a validation error when try create a new sale without required data")
    void testCreateValidationError() throws Exception {
        SaleRequestDTO saleRequestDTO = new SaleRequestDTO();

        mockMvc.perform(post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(saleRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.content").exists());
    }

    @Test
    @DisplayName("Should update a specific sale")
    void testUpdateSuccess() throws Exception {
        Client client = buildClient(1L);
        Vehicle vehicle = buildVehicle(1L, VehicleStatus.SOLD);
        Sale sale = buildSale(1L, vehicle, client, SalesStatus.SOLD);

        VehicleMinimalDTO vehicleMinimalDTO = new VehicleMinimalDTO(1L, "TestChassi", "TestBrand", "TestModel");
        SaleRequestDTO saleRequestDTO = new SaleRequestDTO();
        saleRequestDTO.setClient(client);
        saleRequestDTO.setVehicle(vehicleMinimalDTO);
        saleRequestDTO.setStatus(SalesStatus.SOLD);

        when(saleService.update(anyLong(), any(SaleRequestDTO.class))).thenReturn(sale);

        mockMvc.perform(put("/api/sales/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(saleRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.vehicle.id").value(1L))
                .andExpect(jsonPath("$.client.id").value(1L))
                .andExpect(jsonPath("$.status").value("SOLD"));

        verify(saleService, times(1)).update(anyLong(), any(SaleRequestDTO.class));
    }

    @Test
    @DisplayName("Should throw a validation error when try update a specific sale without required data")
    void testUpdateValidationError() throws Exception {
        SaleRequestDTO saleRequestDTO = new SaleRequestDTO();

        mockMvc.perform(put("/api/sales/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(saleRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.content").exists());
    }

}
