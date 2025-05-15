package com.matheus.VehicleManager.enums;

public enum VehicleChange {

    MANUAL("Manual"),
    AUTOMATIC("Automático"),
    AUTOMATED("Automatizado"),
    CVT("CVT");

    private String vehicleChange;

    private VehicleChange(String vehicleChange) {
        this.vehicleChange = vehicleChange;
    }

    public String getVehicleChange() {
        return this.vehicleChange;
    }

}
