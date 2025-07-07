package com.matheus.VehicleManager.repository;

import com.matheus.VehicleManager.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleRepository extends JpaRepository<Sale, Long> {
}
