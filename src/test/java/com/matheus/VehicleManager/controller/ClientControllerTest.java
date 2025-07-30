package com.matheus.VehicleManager.controller;

import com.matheus.VehicleManager.model.Client;
import com.matheus.VehicleManager.security.JwtAuthenticationFilter;
import com.matheus.VehicleManager.security.JwtUtil;
import com.matheus.VehicleManager.service.ClientService;
import org.apache.catalina.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClientController.class)
@AutoConfigureMockMvc(addFilters = false)
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

    @Test
    void testGetAllClients() throws Exception {
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
    void testGetByEmail() throws Exception {
        Client client = buildClient(1L);
        when(clientService.findByEmail("test@test.com")).thenReturn(client);

        mockMvc.perform(get("/api/clients/email/test@test.com")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@test.com"));
    }

    @Test
    void testSearchClients() throws Exception {
        Client client = buildClient(1L);
        when(clientService.search("TestFirstName")).thenReturn(List.of(client));

        mockMvc.perform(get("/api/clients/search")
                        .param("searchFor", "TestFirstName")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("TestFirstName"));
    }
}