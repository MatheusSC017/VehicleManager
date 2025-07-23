package com.matheus.VehicleManager.repository;

import com.matheus.VehicleManager.model.Financing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FinancingRepository extends JpaRepository<Financing, Long> {

    @Query("SELECT f FROM Financing f WHERE f.vehicle.id = :vehicleId AND f.status <> 'CANCELED'")
    Optional<Financing> findActiveByVehicleId(@Param("vehicleId") Long vehicleId);

}
