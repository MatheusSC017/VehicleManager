package com.matheus.VehicleManager.service;

import com.matheus.VehicleManager.model.FileStore;
import com.matheus.VehicleManager.model.Vehicle;
import com.matheus.VehicleManager.repository.FileRepository;
import com.matheus.VehicleManager.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileServiceTest {

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private FileRepository fileRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private FileService fileService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAll() {
        List<FileStore> fileStores = new ArrayList<>();
        fileStores.add(new FileStore());
        when(fileRepository.findAll()).thenReturn(fileStores);

        List<FileStore> result = fileService.getAll();

        assertEquals(1, result.size());
        verify(fileRepository, times(1)).findAll();
    }

    @Test
    void testGetById() {
        FileStore fileStore = new FileStore();
        fileStore.setId(1L);
        when(fileRepository.getReferenceById(1L)).thenReturn(fileStore);

        FileStore result = fileService.getById(1L);

        assertEquals(1L, result.getId());
        verify(fileRepository, times(1)).getReferenceById(1L);
    }

    @Test
    void testSave() throws IOException {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(1L);
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(fileStorageService.storeFile(file)).thenReturn("path/to/file");

        fileService.save(1L, new MultipartFile[]{file});

        verify(fileStorageService, times(1)).storeFile(file);
        verify(fileRepository, times(1)).save(any(FileStore.class));
    }

    @Test
    void testSaveVehicleNotFound() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.empty());
        MultipartFile file = mock(MultipartFile.class);

        assertThrows(IOException.class, () -> {
            fileService.save(1L, new MultipartFile[]{file});
        });
    }

    @Test
    void testUpdate() throws IOException {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(1L);
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        MultipartFile newFile = mock(MultipartFile.class);
        when(newFile.isEmpty()).thenReturn(false);
        when(fileStorageService.storeFile(newFile)).thenReturn("path/to/new_file");

        FileStore existingFile = new FileStore();
        existingFile.setId(2L);
        existingFile.setVehicle(vehicle);
        when(fileRepository.getReferenceById(2L)).thenReturn(existingFile);

        List<Long> imagesToDelete = new ArrayList<>();
        imagesToDelete.add(2L);

        fileService.update(1L, new MultipartFile[]{newFile}, imagesToDelete);

        verify(fileStorageService, times(1)).storeFile(newFile);
        verify(fileRepository, times(1)).save(any(FileStore.class));
        verify(fileRepository, times(1)).deleteById(2L);
    }
}
