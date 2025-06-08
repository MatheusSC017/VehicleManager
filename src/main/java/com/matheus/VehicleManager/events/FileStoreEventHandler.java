package com.matheus.VehicleManager.events;

import com.matheus.VehicleManager.model.FileStore;
import com.matheus.VehicleManager.service.FileStorageService;
import jakarta.persistence.PreRemove;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class FileStoreEventHandler {

    private final FileStorageService fileStorageService;

    @Autowired
    public FileStoreEventHandler(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PreRemove
    public void onPreRemove(FileStore fileStore) {
        try {
            fileStorageService.deleteFile(fileStore.getPath());
        } catch (IOException e) {
            System.err.println("Could not delete file: " + e.getMessage());
        }
    }
}

