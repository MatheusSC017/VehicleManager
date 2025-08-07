package com.matheus.VehicleManager.repository;

import com.matheus.VehicleManager.dto.VehicleImageResponseDTO;
import com.matheus.VehicleManager.enums.VehicleFuel;
import com.matheus.VehicleManager.enums.VehicleStatus;
import com.matheus.VehicleManager.enums.VehicleType;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.matheus.VehicleManager.model.Vehicle;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    Optional<Vehicle> findByChassi(String chassi);

    @Query("SELECT v FROM Vehicle v WHERE v.vehicleStatus = 'AVAILABLE' AND " +
            "(LOWER(v.brand) LIKE LOWER(CONCAT('%', :searchFor, '%')) OR " +
            "LOWER(v.model) LIKE LOWER(CONCAT('%', :searchFor, '%')) OR " +
            "LOWER(v.plate) LIKE LOWER(CONCAT('%', :searchFor, '%')))")
    List<Vehicle> searchAvailableVehicles(@Param("searchFor") String searchFor);

    @Query("""
    SELECT new com.matheus.VehicleManager.dto.VehicleImageResponseDTO(
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
    Page<VehicleImageResponseDTO> searchVehiclesWithImages(
            @Param("search") String search,
            @Param("status") VehicleStatus status,
            @Param("type") VehicleType type,
            @Param("fuel") VehicleFuel fuel,
            @Param("priceMin") Integer priceMin,
            @Param("priceMax") Integer priceMax,
            @Param("paging") Pageable paging
    );

    @Query("""
    SELECT v FROM Vehicle v
    WHERE (LOWER(v.brand) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(v.model) LIKE LOWER(CONCAT('%', :search, '%')))
      AND (:status IS NULL OR v.vehicleStatus = :status)
      AND (:type IS NULL OR v.vehicleType = :type)
      AND (:fuel IS NULL OR v.vehicleFuel = :fuel)
      AND (:priceMin IS NULL OR v.price >= :priceMin)
      AND (:priceMax IS NULL OR v.price <= :priceMax)
    """)
    Page<Vehicle> searchVehicles(
            @Param("search") String search,
            @Param("status") VehicleStatus status,
            @Param("type") VehicleType type,
            @Param("fuel") VehicleFuel fuel,
            @Param("priceMin") Integer priceMin,
            @Param("priceMax") Integer priceMax,
            @Param("paging") Pageable paging
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
           UPDATE Vehicle v
              SET v.vehicleStatus = :status
            WHERE v.id = :id
           """)
    int updateStatus(@Param("id") Long id, @Param("status") VehicleStatus status);

}
