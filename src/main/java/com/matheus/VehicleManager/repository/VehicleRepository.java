package com.matheus.VehicleManager.repository;

import com.matheus.VehicleManager.dto.VehicleImageDTO;
import com.matheus.VehicleManager.enums.VehicleFuel;
import com.matheus.VehicleManager.enums.VehicleStatus;
import com.matheus.VehicleManager.enums.VehicleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.matheus.VehicleManager.model.Vehicle;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    @Query("""
    SELECT new com.matheus.VehicleManager.dto.VehicleImageDTO(
        v.id,
        v.vehicleType,
        v.vehicleStatus,
        v.model,
        v.brand,
        v.year,
        v.color,
        v.plate,
        v.chassi,
        v.mileage,
        v.price,
        v.vehicleFuel,
        v.vehicleChange,
        v.doors,
        v.motor,
        v.power,
        (
            SELECT fi.path
            FROM FileStore fi
            WHERE fi.vehicle.id = v.id
              AND LOWER(fi.type) = 'image'
            ORDER BY fi.id
            LIMIT 1
        )
    ) FROM Vehicle v
    WHERE (LOWER(v.brand) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(v.model) LIKE LOWER(CONCAT('%', :search, '%')))
      AND (:status IS NULL OR v.vehicleStatus = :status)
      AND (:type IS NULL OR v.vehicleType = :type)
      AND (:fuel IS NULL OR v.vehicleFuel = :fuel)
      AND (:priceMin IS NULL OR v.price >= :priceMin)
      AND (:priceMax IS NULL OR v.price <= :priceMax)
    """)
    Page<VehicleImageDTO> searchVehiclesWithFilters(
            @Param("search") String search,
            @Param("status") VehicleStatus status,
            @Param("type") VehicleType type,
            @Param("fuel") VehicleFuel fuel,
            @Param("priceMin") Integer priceMin,
            @Param("priceMax") Integer priceMax,
            @Param("paging") Pageable paging
    );
}
