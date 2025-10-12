package com.matheus.VehicleManager.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileStorageService {
    String storeFile(MultipartFile file) throws IOException;
    String generatePresignedUrl(String originalFilename, String contentType);
    void deleteFile(String filePath) throws IOException;
}
