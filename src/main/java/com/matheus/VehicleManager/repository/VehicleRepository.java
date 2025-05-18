package com.matheus.VehicleManager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.matheus.VehicleManager.model.Vehicle;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    @Query("SELECT v FROM Vehicle v WHERE LOWER(v.brand) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(v.model) LIKE LOWER(CONCAT('%', :search, '%'))")
    public List<Vehicle> findByBrandAndModelIgnoreCase(String search);

}
