package com.matheus.VehicleManager.repository;

import com.matheus.VehicleManager.enums.VehicleFuel;
import com.matheus.VehicleManager.enums.VehicleStatus;
import com.matheus.VehicleManager.enums.VehicleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.matheus.VehicleManager.model.Vehicle;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;

import java.util.List;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    @Query("""
    SELECT v FROM Vehicle v
    LEFT JOIN FETCH v.images i
    WHERE (LOWER(v.brand) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(v.model) LIKE LOWER(CONCAT('%', :search, '%')))
      AND (:status IS NULL OR v.vehicleStatus = :status)
      AND (:type IS NULL OR v.vehicleType = :type)
      AND (:fuel IS NULL OR v.vehicleFuel = :fuel)
      AND (:priceMin IS NULL OR v.price >= :priceMin)
      AND (:priceMax IS NULL OR v.price <= :priceMax)
    """)
    Page<Vehicle> searchVehiclesWithFilters(
            @Param("search") String search,
            @Param("status") VehicleStatus status,
            @Param("type") VehicleType type,
            @Param("fuel") VehicleFuel fuel,
            @Param("priceMin") Integer priceMin,
            @Param("priceMax") Integer priceMax,
            @Param("paging") Pageable paging
    );

}
