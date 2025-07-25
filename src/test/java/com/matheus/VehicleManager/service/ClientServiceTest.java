package com.matheus.VehicleManager.service;

import com.matheus.VehicleManager.model.Client;
import com.matheus.VehicleManager.model.Vehicle;
import com.matheus.VehicleManager.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientService clientService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindAll() {
        Client client1 = new Client();
        client1.setId(1L);
        client1.setFirstName("TestFirstName");
        client1.setLastName("TestLastName");
        client1.setEmail("Test@test.com");
        client1.setPhone("19 99999-9999");

        Client client2 = new Client();
        client2.setId(2L);
        client2.setFirstName("TestFirstName2");
        client2.setLastName("TestLastName2");
        client2.setEmail("Test2@test.com");
        client2.setPhone("19 88888-8888");

        List<Client> searchedClients = new ArrayList<>();
        searchedClients.add(client1);
        searchedClients.add(client2);

        Pageable paging = PageRequest.of(0, 20);
        Page<Client> clientPage = new PageImpl<>(searchedClients, paging, searchedClients.size());
        when(clientRepository.findAll(any(Pageable.class))).thenReturn(clientPage);

        Page<Client> foundVehicles = clientService.findAll(0, 20);

        assertEquals(2, foundVehicles.getContent().size());
        assertEquals(1L, foundVehicles.getContent().get(0).getId());
        assertEquals("TestFirstName", foundVehicles.getContent().get(0).getFirstName());
        assertEquals("TestLastName", foundVehicles.getContent().get(0).getLastName());
        assertEquals("Test@test.com", foundVehicles.getContent().get(0).getEmail());
        assertEquals("19 99999-9999", foundVehicles.getContent().get(0).getPhone());
        assertEquals(2L, foundVehicles.getContent().get(1).getId());
        assertEquals("TestFirstName2", foundVehicles.getContent().get(1).getFirstName());
        assertEquals("TestLastName2", foundVehicles.getContent().get(1).getLastName());
        assertEquals("Test2@test.com", foundVehicles.getContent().get(1).getEmail());
        assertEquals("19 88888-8888", foundVehicles.getContent().get(1).getPhone());
        verify(clientRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void testSearch() {
        Client client1 = new Client();
        client1.setId(1L);
        client1.setFirstName("TestFirstName");
        client1.setLastName("TestLastName");
        client1.setEmail("Test@test.com");
        client1.setPhone("19 99999-9999");

        Client client2 = new Client();
        client2.setId(2L);
        client2.setFirstName("TestFirstName2");
        client2.setLastName("TestLastName2");
        client2.setEmail("Test2@test.com");
        client2.setPhone("19 88888-8888");

        List<Client> searchedClients = new ArrayList<>();
        searchedClients.add(client1);
        searchedClients.add(client2);

        when(clientRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrPhoneContaining("Test", "Test", "Test"))
                .thenReturn(searchedClients);

        List<Client> foundVehicles = clientService.search("Test");

        assertEquals(2, foundVehicles.size());
        assertEquals(1L, foundVehicles.get(0).getId());
        assertEquals("TestFirstName", foundVehicles.get(0).getFirstName());
        assertEquals("TestLastName", foundVehicles.get(0).getLastName());
        assertEquals("Test@test.com", foundVehicles.get(0).getEmail());
        assertEquals("19 99999-9999", foundVehicles.get(0).getPhone());
        assertEquals(2L, foundVehicles.get(1).getId());
        assertEquals("TestFirstName2", foundVehicles.get(1).getFirstName());
        assertEquals("TestLastName2", foundVehicles.get(1).getLastName());
        assertEquals("Test2@test.com", foundVehicles.get(1).getEmail());
        assertEquals("19 88888-8888", foundVehicles.get(1).getPhone());
        verify(clientRepository, times(1))
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrPhoneContaining(anyString(), anyString(), anyString());
    }

    @Test
    void testFindByEmail() {
        String email = "Test@test.com";
        Client client = new Client();
        client.setEmail(email);

        when(clientRepository.findByEmail(email)).thenReturn(client);

        Client foundClient = clientService.findByEmail(email);

        assertEquals(email, foundClient.getEmail());
        verify(clientRepository, times(1)).findByEmail(email);
    }

    @Test
    void testGetById() {
        Long clientId = 1L;

        Client client = new Client();
        client.setId(clientId);
        client.setFirstName("TestFirstName");
        client.setLastName("TestLastName");
        client.setEmail("Test@test.com");
        client.setPhone("19 99999-9999");

        when(clientRepository.getReferenceById(clientId)).thenReturn(client);

        Client foundClient = clientService.getById(clientId);

        assertEquals(clientId, foundClient.getId());
        assertEquals("TestFirstName", foundClient.getFirstName());
        assertEquals("TestLastName", foundClient.getLastName());
        assertEquals("Test@test.com", foundClient.getEmail());
        assertEquals("19 99999-9999", foundClient.getPhone());
        verify(clientRepository, times(1)).getReferenceById(clientId);
    }

    @Test
    void testCreate() {
        Client client = new Client();
        client.setId(1L);
        client.setFirstName("TestFirstName");
        client.setLastName("TestLastName");
        client.setEmail("Test@test.com");
        client.setPhone("19 99999-9999");

        when(clientRepository.save(any(Client.class))).thenReturn(client);

        Client createdClient = clientService.create(client);

        assertEquals(1L, createdClient.getId());
        assertEquals("TestFirstName", createdClient.getFirstName());
        assertEquals("TestLastName", createdClient.getLastName());
        assertEquals("Test@test.com", createdClient.getEmail());
        assertEquals("19 99999-9999", createdClient.getPhone());
        verify(clientRepository, times(1)).save(any(Client.class));
    }

    @Test
    void testUpdate() {
        Long clientId = 1L;
        Client client = new Client();
        client.setFirstName("TestFirstName");
        client.setLastName("TestLastName");
        client.setEmail("Test@test.com");
        client.setPhone("19 99999-9999");

        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Client createdClient = clientService.update(clientId, client);

        assertEquals(1L, createdClient.getId());
        assertEquals("TestFirstName", createdClient.getFirstName());
        assertEquals("TestLastName", createdClient.getLastName());
        assertEquals("Test@test.com", createdClient.getEmail());
        assertEquals("19 99999-9999", createdClient.getPhone());
        verify(clientRepository, times(1)).save(any(Client.class));
    }

    @Test
    void testDelete() {
        Long clientId = 1L;

        Client client = new Client();
        client.setId(clientId);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        doNothing().when(clientRepository).deleteById(clientId);
        clientService.delete(clientId);
        verify(clientRepository, times(1)).findById(clientId);
        verify(clientRepository, times(1)).delete(client);
    }

}
