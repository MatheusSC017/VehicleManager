package com.matheus.VehicleManager.enums;

public enum VehicleChange {

    MANUAL("Manual"),
    AUTOMATIC("Automatic"),
    AUTOMATED("Automated"),
    CVT("CVT");

    private String vehicleChange;

    private VehicleChange(String vehicleChange) { this.vehicleChange = vehicleChange; }

}
