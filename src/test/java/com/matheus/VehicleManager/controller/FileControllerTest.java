package com.matheus.VehicleManager.controller;

import com.matheus.VehicleManager.enums.FileType;
import com.matheus.VehicleManager.model.FileStore;
import com.matheus.VehicleManager.model.Vehicle;
import com.matheus.VehicleManager.security.JwtAuthenticationFilter;
import com.matheus.VehicleManager.security.JwtUtil;
import com.matheus.VehicleManager.service.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FileController.class)
@AutoConfigureMockMvc(addFilters = false)
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FileService fileService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private FileStore createFileStore() {
        FileStore file = new FileStore();
        file.setId(1L);
        file.setPath("images/test.jpg");
        file.setType(FileType.IMAGE);

        Vehicle vehicle = new Vehicle();
        vehicle.setId(10L);
        file.setVehicle(vehicle);

        return file;
    }

    @Test
    void testGetAll() throws Exception {
        FileStore fileStore = createFileStore();
        List<FileStore> files = Collections.singletonList(fileStore);

        when(fileService.getAll()).thenReturn(files);

        mockMvc.perform(get("/api/files"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(fileStore.getId()))
                .andExpect(jsonPath("$[0].path").value(fileStore.getPath()))
                .andExpect(jsonPath("$[0].type").value("IMAGE"))
                .andExpect(jsonPath("$[0].vehicle").value(fileStore.getVehicle().getId()));
    }

    @Test
    void testGetById() throws Exception {
        FileStore fileStore = createFileStore();

        when(fileService.getById(1L)).thenReturn(fileStore);

        mockMvc.perform(get("/api/files/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(fileStore.getId()))
                .andExpect(jsonPath("$.path").value(fileStore.getPath()))
                .andExpect(jsonPath("$.type").value("IMAGE"))
                .andExpect(jsonPath("$.vehicle").value(fileStore.getVehicle().getId()));
    }

    @Test
    void testInsert() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "imagesInput", "test.jpg", "image/jpeg", "test image".getBytes());

        doNothing().when(fileService).save(eq(10L), any(MultipartFile[].class));

        mockMvc.perform(multipart("/api/files")
                        .file(image)
                        .param("vehicleId", "10"))
                .andExpect(status().isCreated());
    }

    @Test
    void testInsertWithIOException() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "imagesInput", "test.jpg", "image/jpeg", "test image".getBytes());

        doNothing().when(fileService).save(eq(10L), any(MultipartFile[].class));
        doThrow(new IOException("Failed to save"))
                .when(fileService)
                .save(eq(10L), any(MultipartFile[].class));

        mockMvc.perform(multipart("/api/files")
                        .file(image)
                        .param("vehicleId", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Failed to save"));
    }

    @Test
    void testUpdate() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "imagesInput", "update.jpg", "image/jpeg", "update image".getBytes());

        doNothing().when(fileService).update(eq(10L), any(MultipartFile[].class), anyList());

        mockMvc.perform(multipart("/api/files/1")
                        .file(image)
                        .param("vehicleId", "10")
                        .param("selectedImages", "1", "2")
                        .with(request -> { request.setMethod("PUT"); return request; }))
                .andExpect(status().isOk());
    }

    @Test
    void testUpdateWithIOException() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "imagesInput", "update.jpg", "image/jpeg", "update image".getBytes());

        doThrow(new IOException("Failed to update"))
                .when(fileService)
                .update(eq(10L), any(MultipartFile[].class), anyList());

        mockMvc.perform(multipart("/api/files/1")
                        .file(image)
                        .param("vehicleId", "10")
                        .param("selectedImages", "1", "2")
                        .with(request -> { request.setMethod("PUT"); return request; }))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Failed to update"));
    }
}
