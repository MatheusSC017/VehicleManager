package com.matheus.VehicleManager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matheus.VehicleManager.model.Client;
import com.matheus.VehicleManager.security.JwtAuthenticationFilter;
import com.matheus.VehicleManager.security.JwtUtil;
import com.matheus.VehicleManager.service.ClientService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClientController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClientService clientService;

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

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Should return all clients with pagination")
    void testGetAll() throws Exception {
        Client client = buildClient(1L);
        when(clientService.findAll(0, 10))
                .thenReturn(new PageImpl<>(List.of(client), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/clients")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].firstName").value("TestFirstName"));
    }

    @Test
    @DisplayName("Should return all clients based on a search")
    void testSearch() throws Exception {
        Client client = buildClient(1L);
        when(clientService.search("TestFirstName")).thenReturn(List.of(client));

        mockMvc.perform(get("/api/clients/search")
                        .param("searchFor", "TestFirstName")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("TestFirstName"));
    }

    @Test
    @DisplayName("Should return a specific client based on email")
    void testGetByEmail() throws Exception {
        Client client = buildClient(1L);
        when(clientService.findByEmail("test@test.com")).thenReturn(client);

        mockMvc.perform(get("/api/clients/email/test@test.com")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@test.com"));
    }

    @Test
    @DisplayName("Should return a specific client based on id")
    void testGetById() throws Exception {
        Client client = buildClient(1L);
        when(clientService.getById(1L)).thenReturn(client);

        mockMvc.perform(get("/api/clients/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("Should create a new client")
    void testInsertSuccess() throws Exception {
        Client client = buildClient(1L);

        when(clientService.create(any(Client.class))).thenReturn(client);

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(client)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value(client.getFirstName()));
    }

    @Test
    @DisplayName("Should throw a validation error when try create a new client without required data")
    void testInsertValidationError() throws Exception {
        Client client = new Client();

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(client)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.content").exists());
    }

    @Test
    @DisplayName("Should update a specific client")
    void testUpdateSuccess() throws Exception {
        Client client = buildClient(1L);

        when(clientService.update(eq(1L), any(Client.class))).thenReturn(client);

        mockMvc.perform(put("/api/clients/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(client)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value(client.getFirstName()));
    }

    @Test
    @DisplayName("Should throw a validation error when try update a specific client without required data")
    void testUpdateValidationError() throws Exception {
        Client client = new Client();

        mockMvc.perform(put("/api/clients/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(client)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.content").exists());
    }

    @Test
    @DisplayName("Should delete a specific client")
    void testDeleteSuccess() throws Exception {
        doNothing().when(clientService).delete(1L);

        mockMvc.perform(delete("/api/clients/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should throw an error during delete call if client doesn't exists")
    void testDeleteFailure() throws Exception {
        doThrow(new RuntimeException("Client not found")).when(clientService).delete(1L);

        mockMvc.perform(delete("/api/clients/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.error").value("Client not found"));
    }

}