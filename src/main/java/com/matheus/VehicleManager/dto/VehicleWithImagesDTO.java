package com.matheus.VehicleManager.dto;

import com.matheus.VehicleManager.model.FileStore;
import com.matheus.VehicleManager.model.Vehicle;

import java.util.List;

public class VehicleWithImagesDTO {

    private Vehicle vehicle;
    private List<FileStore> images;

    public VehicleWithImagesDTO(Vehicle vehicle, List<FileStore> images) {
        this.vehicle = vehicle;
        this.images = images;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public List<FileStore> getImages() {
        return images;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public void setImages(List<FileStore> images) {
        this.images = images;
    }

}
