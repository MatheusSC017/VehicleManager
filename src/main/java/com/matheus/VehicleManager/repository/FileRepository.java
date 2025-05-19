package com.matheus.VehicleManager.repository;

import com.matheus.VehicleManager.model.FileStore;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<FileStore, Long> {
}
