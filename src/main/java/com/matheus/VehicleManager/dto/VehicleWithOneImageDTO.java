package com.matheus.VehicleManager.dto;

import com.matheus.VehicleManager.model.FileStore;
import com.matheus.VehicleManager.model.Vehicle;

public class VehicleWithOneImageDTO {

    private Vehicle vehicle;
    private FileStore image;

    public VehicleWithOneImageDTO(Vehicle vehicle, FileStore image) {
        this.vehicle = vehicle;
        this.image = image;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public FileStore getImage() {
        return image;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public void setImage(FileStore image) {
        this.image = image;
    }

}
