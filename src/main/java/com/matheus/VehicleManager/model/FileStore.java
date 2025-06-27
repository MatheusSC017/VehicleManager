package com.matheus.VehicleManager.model;

import com.matheus.VehicleManager.enums.FileType;
import com.matheus.VehicleManager.events.FileStoreEventHandler;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@EntityListeners(FileStoreEventHandler.class)
public class FileStore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String path;

    @Enumerated(EnumType.STRING)
    private FileType type;

    @ManyToOne
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    private LocalDate createdAt;
    private LocalDate updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDate.now();
        this.updatedAt = LocalDate.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDate.now();
    }

    public Long getId() {
        return id;
    }

    public String getPath() {
        return path;
    }

    public FileType getType() {
        return type;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setType(FileType type) {
        this.type = type;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

}
