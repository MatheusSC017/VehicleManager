package com.matheus.VehicleManager.repository;

import com.matheus.VehicleManager.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {
    List<Sale> findByVehicleId(Long vehicleId);
}
