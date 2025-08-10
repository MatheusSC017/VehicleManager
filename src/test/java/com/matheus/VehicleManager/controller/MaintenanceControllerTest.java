package com.matheus.VehicleManager.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.matheus.VehicleManager.dto.MaintenanceRequestDTO;
import com.matheus.VehicleManager.enums.VehicleFuel;
import com.matheus.VehicleManager.enums.VehicleStatus;
import com.matheus.VehicleManager.enums.VehicleType;
import com.matheus.VehicleManager.model.Maintenance;
import com.matheus.VehicleManager.model.Vehicle;
import com.matheus.VehicleManager.security.JwtAuthenticationFilter;
import com.matheus.VehicleManager.security.JwtUtil;
import com.matheus.VehicleManager.service.MaintenanceService;
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

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MaintenanceController.class)
@AutoConfigureMockMvc(addFilters = false)
public class MaintenanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MaintenanceService maintenanceService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

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

    private Maintenance buildMaintenance(Long maintenanceId, Vehicle vehicle) {
        Maintenance maintenance = new Maintenance();
        maintenance.setId(maintenanceId);
        maintenance.setVehicle(vehicle);
        maintenance.setAdditionalInfo("TestAdditionalInfo");
        maintenance.setStartDate(LocalDate.now().minusDays(4));
        return maintenance;
    }

    private static String asJsonString(final Object obj) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(obj);
    }

    @Test
    @DisplayName("Should return all maintenances with pagination")
    void testGetAll() throws Exception {
        Maintenance maintenance1 = buildMaintenance(1L, new Vehicle());
        Maintenance maintenance2 = buildMaintenance(2L, new Vehicle());
        when(maintenanceService.findAll(0, 10))
                .thenReturn(new PageImpl<>(List.of(maintenance1, maintenance2), PageRequest.of(0, 10), 1));


        mockMvc.perform(get("/api/maintenances")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].additionalInfo").value("TestAdditionalInfo"))
                .andExpect(jsonPath("$.content[1].id").value(2L))
                .andExpect(jsonPath("$.content[1].additionalInfo").value("TestAdditionalInfo"));
    }

    @Test
    @DisplayName("Should return all maintenances based on vehicle id")
    void testGetAllByVehicle() throws Exception {
        Vehicle vehicle = buildVehicle(1L, VehicleStatus.SOLD);

        Maintenance maintenance1 = buildMaintenance(1L, vehicle);
        Maintenance maintenance2 = buildMaintenance(2L, vehicle);
        when(maintenanceService.findAllByVehicleId(1L)).thenReturn(List.of(maintenance1, maintenance2));

        mockMvc.perform(get("/api/maintenances/vehicle/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].vehicle.id").value(1L))
                .andExpect(jsonPath("$[0].additionalInfo").value("TestAdditionalInfo"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].vehicle.id").value(1L))
                .andExpect(jsonPath("$[1].additionalInfo").value("TestAdditionalInfo"));
    }

    @Test
    @DisplayName("Should return a specific maintenance based on id")
    void testGet() throws Exception {
        Maintenance maintenance = buildMaintenance(1L, new Vehicle());
        when(maintenanceService.findById(1L)).thenReturn(maintenance);

        mockMvc.perform(get("/api/maintenances/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.additionalInfo").value("TestAdditionalInfo"));
    }

    @Test
    @DisplayName("Should create a new maintenance")
    void testCreateSuccess() throws Exception {
        Vehicle vehicle = buildVehicle(1L, VehicleStatus.SOLD);
        Maintenance maintenance = buildMaintenance(1L, vehicle);

        MaintenanceRequestDTO maintenanceRequestDTO = new MaintenanceRequestDTO();
        maintenanceRequestDTO.setVehicleId(1L);
        maintenanceRequestDTO.setAdditionalInfo("TestAdditionalInfo");

        when(maintenanceService.create(1L, "TestAdditionalInfo")).thenReturn(maintenance);

        mockMvc.perform(post("/api/maintenances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(maintenanceRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.additionalInfo").value("TestAdditionalInfo"))
                .andExpect(jsonPath("$.vehicle.id").value(1L));

        verify(maintenanceService, times(1)).create(1L, "TestAdditionalInfo");
    }

    @Test
    @DisplayName("Should throw a validation error when try create a new maintenance without required data")
    void testCreateValidationError() throws Exception {
        MaintenanceRequestDTO maintenanceRequestDTO = new MaintenanceRequestDTO();

        mockMvc.perform(post("/api/maintenances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(maintenanceRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    @DisplayName("Should update a specific maintenance")
    void testDeleteSuccess() throws Exception {
        doNothing().when(maintenanceService).delete(1L);

        mockMvc.perform(delete("/api/maintenances/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(maintenanceService, times(1)).delete(anyLong());
    }

    @Test
    @DisplayName("Should throw a validation error when try update a specific maintenance without required data")
    void testUpdateValidationError() throws Exception {
        doThrow(new RuntimeException("Maintenance not found")).when(maintenanceService).delete(1L);

        mockMvc.perform(delete("/api/maintenances/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.error").value("Maintenance not found"));
    }

}
