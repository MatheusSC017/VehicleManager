package com.matheus.VehicleManager.service;

import com.matheus.VehicleManager.model.Client;
import com.matheus.VehicleManager.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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

    private Client buildClient(Long id) {
        Client client = new Client();
        client.setId(id);
        client.setFirstName("TestFirstName");
        client.setLastName("TestLastName");
        client.setEmail("Test@test.com");
        client.setPhone("19 88888-8888");
        return client;
    }

    @Test
    @DisplayName("Should return all clients with pagination")
    void testFindAll() {
        Client client1 = buildClient(1L);
        Client client2 = buildClient(2L);
        List<Client> searchedClients = List.of(client1, client2);

        Pageable paging = PageRequest.of(0, 20);
        Page<Client> clientPage = new PageImpl<>(searchedClients, paging, searchedClients.size());
        when(clientRepository.findAll(any(Pageable.class))).thenReturn(clientPage);

        Page<Client> foundClients = clientService.findAll(0, 20);

        assertEquals(2, foundClients.getContent().size());
        assertEquals(client1, foundClients.getContent().get(0));
        assertEquals(client2, foundClients.getContent().get(1));
        verify(clientRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Should return all clients based on a search")
    void testSearch() {
        Client client1 = buildClient(1L);
        Client client2 = buildClient(2L);
        List<Client> searchedClients = List.of(client1, client2);

        when(clientRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrPhoneContaining("Test", "Test", "Test"))
                .thenReturn(searchedClients);
        List<Client> foundClients = clientService.search("Test");

        assertEquals(2, foundClients.size());
        assertEquals(client1, foundClients.get(0));
        assertEquals(client2, foundClients.get(1));
        verify(clientRepository, times(1))
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrPhoneContaining(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should return a specific client based on email")
    void testFindByEmail() {
        Client client = buildClient(1L);

        when(clientRepository.findByEmail(anyString())).thenReturn(Optional.of(client));
        Client foundClient = clientService.findByEmail("Test@test.com");

        assertEquals("Test@test.com", foundClient.getEmail());
        assertEquals(client, foundClient);
        verify(clientRepository, times(1)).findByEmail(anyString());
    }

    @Test
    @DisplayName("Should return a specific client based on id")
    void testGetById() {
        Client client = buildClient(1L);

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        Client foundClient = clientService.getById(1L);

        assertEquals(client, foundClient);
        verify(clientRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should create a new client")
    void testCreate() {
        Client client = buildClient(1L);

        when(clientRepository.save(any(Client.class))).thenReturn(client);
        Client createdClient = clientService.create(client);

        assertEquals(client, createdClient);
        verify(clientRepository, times(1)).save(any(Client.class));
    }

    @Test
    @DisplayName("Should update a specific client")
    void testUpdate() {
        Client client = buildClient(1L);

        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Client updatedClient = clientService.update(1L, client);

        assertEquals(client, updatedClient);
        verify(clientRepository, times(1)).save(any(Client.class));
    }

    @Test
    @DisplayName("Should delete a specific client")
    void testDelete() {
        Client client = buildClient(1L);

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        doNothing().when(clientRepository).deleteById(1L);
        clientService.delete(1L);

        verify(clientRepository, times(1)).findById(1L);
        verify(clientRepository, times(1)).delete(client);
    }

}
