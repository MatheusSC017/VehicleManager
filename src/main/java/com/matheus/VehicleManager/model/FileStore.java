package com.matheus.VehicleManager.model;

import com.matheus.VehicleManager.enums.FileType;
import jakarta.persistence.*;

@Entity
public class FileStore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;

    private String contentType;

    private String path;

    @Enumerated(EnumType.STRING)
    private FileType type;

    @ManyToOne
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    public Long getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContentType() {
        return contentType;
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

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
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
