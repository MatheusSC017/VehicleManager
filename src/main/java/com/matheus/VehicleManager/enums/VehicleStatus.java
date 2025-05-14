package com.matheus.VehicleManager.enums;

public enum VehicleStatus {

    AVAILABLE("Available"),
    REFURBISHED("Refurbished"),
    SOLD("Sold"),
    MAINTENACE("Maintenance");

    private String vehicleStatus;

    private VehicleStatus(String vehicleStatus) { this.vehicleStatus = vehicleStatus; }

}
