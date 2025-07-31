package com.matheus.VehicleManager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matheus.VehicleManager.dto.VehicleImageResponseDTO;
import com.matheus.VehicleManager.dto.VehicleImagesResponseDTO;
import com.matheus.VehicleManager.dto.VehicleRequestDTO;
import com.matheus.VehicleManager.enums.VehicleChange;
import com.matheus.VehicleManager.enums.VehicleFuel;
import com.matheus.VehicleManager.enums.VehicleStatus;
import com.matheus.VehicleManager.enums.VehicleType;
import com.matheus.VehicleManager.model.Vehicle;
import com.matheus.VehicleManager.security.JwtAuthenticationFilter;
import com.matheus.VehicleManager.security.JwtUtil;
import com.matheus.VehicleManager.service.VehicleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VehicleController.class)
@AutoConfigureMockMvc(addFilters = false)
public class VehicleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VehicleService vehicleService;

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

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Should return a specific vehicle based on the chassi value")
    void testFindByChassi() throws Exception {
        Vehicle vehicle = buildVehicle(1L, VehicleStatus.AVAILABLE);
        when(vehicleService.findByChassi("TestChassi123")).thenReturn(vehicle);

        mockMvc.perform(get("/api/vehicles/chassi/TestChassi123")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chassi").value("TestChassi123"));

        verify(vehicleService, times(1)).findByChassi("TestChassi123");
    }

    @Test
    @DisplayName("Should return a specific vehicle with the images")
    void testGetVehicleWithImagesById() throws Exception {
        VehicleImagesResponseDTO vehicleImagesResponseDTO = new VehicleImagesResponseDTO(
            1L, VehicleType.CAR, VehicleStatus.SOLD, "TestModel", "TestBrand", 1999, "Red",
            "ABC12D3", "TestChassi", new BigDecimal(10000), new BigDecimal(50000), VehicleFuel.HYBRID,
            VehicleChange.AUTOMATED, 4, "2.0", "120cv", new ArrayList<>()
        );

        when(vehicleService.getVehicleWithImagesById(1L)).thenReturn(vehicleImagesResponseDTO);
        mockMvc.perform(get("/api/vehicles/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.chassi").value("TestChassi"))
                .andExpect(jsonPath("$.images").value(new ArrayList<>()));

        verify(vehicleService, times(1)).getVehicleWithImagesById(1L);
    }

    @Test
    @DisplayName("Should return all vehicles with pagination")
    void testGetFilteredVehicles() throws Exception {
        Vehicle vehicle1 = buildVehicle(1L, VehicleStatus.AVAILABLE);
        Vehicle vehicle2 = buildVehicle(2L, VehicleStatus.AVAILABLE);
        List<Vehicle> searchedVehicles = new ArrayList<>();
        searchedVehicles.add(vehicle1);
        searchedVehicles.add(vehicle2);

        Pageable paging = PageRequest.of(0, 20);
        Page<Vehicle> vehiclePage = new PageImpl<>(searchedVehicles, paging, searchedVehicles.size());
        when(vehicleService.getFilteredVehicles(
                anyString(),
                any(),
                any(),
                any(),
                anyInt(),
                anyInt(),
                any(Pageable.class)
        )).thenReturn(vehiclePage);

        mockMvc.perform(get("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].vehicleType").value("CAR"))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].vehicleStatus").value("AVAILABLE"))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0))
                .andExpect(jsonPath("$.pageable.pageSize").value(20));

        verify(vehicleService, times(1)).getFilteredVehicles(
                anyString(),
                any(),
                any(),
                any(),
                anyInt(),
                anyInt(),
                any(Pageable.class)
        );
    }

    @Test
    @DisplayName("Should return all vehicles with pagination and one image by register")
    void tetFilteredVehiclesWithOneImage() throws Exception {
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
        when(vehicleService.getFilteredVehiclesWithOneImage(
                anyString(),
                any(),
                any(),
                any(),
                anyInt(),
                anyInt(),
                any(Pageable.class)
        )).thenReturn(vehiclePage);

        mockMvc.perform(get("/api/vehicles/images")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].vehicleType").value("CAR"))
                .andExpect(jsonPath("$.content[0].image").value("TestMaverickUrlImage"))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].vehicleStatus").value("MAINTENANCE"))
                .andExpect(jsonPath("$.content[1].image").value("TestMustangUrlImage"))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0))
                .andExpect(jsonPath("$.pageable.pageSize").value(20));

       verify(vehicleService, times(1)).getFilteredVehiclesWithOneImage(
                anyString(),
                any(),
                any(),
                any(),
                anyInt(),
                anyInt(),
                any(Pageable.class)
        );
    }

    @Test
    @DisplayName("Should return all available vehicles")
    void testSearchAvailableVehicles() throws Exception {
        Vehicle vehicle1 = buildVehicle(1L, VehicleStatus.AVAILABLE);
        Vehicle vehicle2 = buildVehicle(2L, VehicleStatus.AVAILABLE);
        List<Vehicle> searchedVehicles = new ArrayList<>();
        searchedVehicles.add(vehicle1);
        searchedVehicles.add(vehicle2);
        when(vehicleService.searchAvailableVehicles("Ford")).thenReturn(searchedVehicles);

        mockMvc.perform(get("/api/vehicles/search?searchFor=Ford")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].vehicleType").value("CAR"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].vehicleStatus").value("AVAILABLE"));

        verify(vehicleService, times(1)).searchAvailableVehicles("Ford");
    }

    @Test
    @DisplayName("Should create a new vehicle")
    void testCreateSuccess() throws Exception {
        VehicleRequestDTO vehicleRequestDTO = new VehicleRequestDTO();
        vehicleRequestDTO.setVehicleType(VehicleType.CAR);
        vehicleRequestDTO.setModel("TestModel");
        vehicleRequestDTO.setBrand("TestBrand");
        vehicleRequestDTO.setYear(1999);
        vehicleRequestDTO.setColor("Test Color");
        vehicleRequestDTO.setMileage(new BigDecimal("9999.99"));
        vehicleRequestDTO.setChassi("TestChassi123");
        vehicleRequestDTO.setPrice(new BigDecimal("99999.99"));
        vehicleRequestDTO.setVehicleFuel(VehicleFuel.HYBRID);
        vehicleRequestDTO.setDoors(2);

        Vehicle vehicle = buildVehicle(1L, VehicleStatus.AVAILABLE);
        when(vehicleService.create(any(VehicleRequestDTO.class))).thenReturn(vehicle);

        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(vehicleRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.chassi").value("TestChassi123"));

        verify(vehicleService, times(1)).create(any(VehicleRequestDTO.class));
    }

    @Test
    @DisplayName("Should throw a validation error when try create a new vehicle without required data")
    void testCreateValidationError() throws Exception {
        VehicleRequestDTO vehicleRequestDTO = new VehicleRequestDTO();

        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(vehicleRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.content").exists());
    }

    @Test
    @DisplayName("Should update a specific vehicle")
    void testUpdateSuccess() throws Exception {
        VehicleRequestDTO vehicleRequestDTO = new VehicleRequestDTO();
        vehicleRequestDTO.setVehicleType(VehicleType.CAR);
        vehicleRequestDTO.setModel("TestNewModel");
        vehicleRequestDTO.setBrand("TestNewBrand");
        vehicleRequestDTO.setYear(1999);
        vehicleRequestDTO.setColor("Test New Color");
        vehicleRequestDTO.setMileage(new BigDecimal("9999.99"));
        vehicleRequestDTO.setChassi("TestNewChassi");
        vehicleRequestDTO.setPrice(new BigDecimal("99999.99"));
        vehicleRequestDTO.setVehicleFuel(VehicleFuel.HYBRID);
        vehicleRequestDTO.setDoors(2);

        Vehicle vehicle = buildVehicle(1L, VehicleStatus.AVAILABLE);
        when(vehicleService.update(anyLong(), any(VehicleRequestDTO.class))).thenReturn(vehicle);

        mockMvc.perform(put("/api/vehicles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(vehicleRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.model").value("TestModel"))
                .andExpect(jsonPath("$.brand").value("TestBrand"))
                .andExpect(jsonPath("$.color").value("Test Color"))
                .andExpect(jsonPath("$.chassi").value("TestChassi123"));

        verify(vehicleService, times(1)).update(anyLong(), any(VehicleRequestDTO.class));
    }

    @Test
    @DisplayName("Should throw a validation error when try update a specific vehicle without required data")
    void testUpdateValidationError() throws Exception {
        VehicleRequestDTO vehicleRequestDTO = new VehicleRequestDTO();

        mockMvc.perform(put("/api/vehicles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(vehicleRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.content").exists());
    }

    @Test
    @DisplayName("Should delete a specific vehicle")
    void testDeleteSuccess() throws Exception {
        doNothing().when(vehicleService).delete(1L);

        mockMvc.perform(delete("/api/vehicles/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should throw an error during delete call if vehicle doesn't exists")
    void testDeleteFailure() throws Exception {
        doThrow(new RuntimeException("Vehicle not found")).when(vehicleService).delete(1L);

        mockMvc.perform(delete("/api/vehicles/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.error").value("Vehicle not found"));
    }

}
