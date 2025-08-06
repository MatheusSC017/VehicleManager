package com.matheus.VehicleManager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.matheus.VehicleManager.dto.FinancingRequestDTO;
import com.matheus.VehicleManager.dto.FinancingStatusRequestDTO;
import com.matheus.VehicleManager.dto.VehicleMinimalDTO;
import com.matheus.VehicleManager.enums.*;
import com.matheus.VehicleManager.model.*;
import com.matheus.VehicleManager.security.JwtAuthenticationFilter;
import com.matheus.VehicleManager.security.JwtUtil;
import com.matheus.VehicleManager.service.FinancingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FinancingController.class)
@AutoConfigureMockMvc(addFilters = false)
public class FinancingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FinancingService financingService;

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

    private static String asJsonString(final Object obj) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Should return all financings with pagination")
    void testGetAll() throws Exception {
        Financing financing1 = buildFinancing(1L, new Vehicle(), new Client(), FinancingStatus.DRAFT);
        Financing financing2 = buildFinancing(2L, new Vehicle(), new Client(), FinancingStatus.ACTIVE);
        when(financingService.getAll(0, 10))
                .thenReturn(new PageImpl<>(List.of(financing1, financing2), PageRequest.of(0, 10), 1));


        mockMvc.perform(get("/api/financings")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].financingStatus").value("DRAFT"))
                .andExpect(jsonPath("$.content[1].id").value(2L))
                .andExpect(jsonPath("$.content[1].financingStatus").value("ACTIVE"));
    }

    @Test
    @DisplayName("Should return a specific financing based on id")
    void testGet() throws Exception {
        Financing financing = buildFinancing(1L, new Vehicle(), new Client(), FinancingStatus.DRAFT);
        when(financingService.getById(1L)).thenReturn(financing);

        mockMvc.perform(get("/api/financings/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.financingStatus").value("DRAFT"));
    }

    @Test
    @DisplayName("Should return a specific financing based on vehicle id with status not canceled")
    void testGetByVehicleIdNotCanceled() throws Exception {
        Vehicle vehicle = buildVehicle(1L, VehicleStatus.SOLD);
        Financing financing = buildFinancing(1L, vehicle, new Client(), FinancingStatus.DRAFT);
        when(financingService.getByVehicleIdNotCanceled(1L)).thenReturn(Optional.of(financing));

        mockMvc.perform(get("/api/financings/vehicle/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.vehicle.id").value(1L))
                .andExpect(jsonPath("$.financingStatus").value("DRAFT"));
    }

    @Test
    @DisplayName("Should create a new financing")
    void testCreateSuccess() throws Exception {
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

        when(financingService.create(any(FinancingRequestDTO.class))).thenReturn(financing);

        mockMvc.perform(post("/api/financings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(financingRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.vehicle.id").value(1L))
                .andExpect(jsonPath("$.client.id").value(1L));

        verify(financingService, times(1)).create(any(FinancingRequestDTO.class));
    }

    @Test
    @DisplayName("Should throw a validation error when try create a new financing without required data")
    void testCreateValidationError() throws Exception {
        FinancingRequestDTO financingRequestDTO = new FinancingRequestDTO();

        mockMvc.perform(post("/api/financings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(financingRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.content").exists());
    }

    @Test
    @DisplayName("Should update a specific financing status")
    void testUpdateStatusSuccess() throws Exception {
        FinancingStatusRequestDTO financingStatusRequestDTO = new FinancingStatusRequestDTO(FinancingStatus.ACTIVE);
        doNothing().when(financingService).updateStatus(1L, financingStatusRequestDTO.status());

        mockMvc.perform(patch("/api/financings/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(financingStatusRequestDTO)))
                .andExpect(status().isNoContent());

        verify(financingService, times(1)).updateStatus(1L, financingStatusRequestDTO.status());
    }

}
