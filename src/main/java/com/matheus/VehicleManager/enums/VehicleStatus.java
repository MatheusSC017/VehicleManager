package com.matheus.VehicleManager.enums;

public enum VehicleStatus {

    AVAILABLE("Disponível"),
    REFURBISHED("Reservado"),
    SOLD("Vendido"),
    MAINTENACE("Manutenção");

    private String vehicleStatus;

    private VehicleStatus(String vehicleStatus) {
        this.vehicleStatus = vehicleStatus;
    }

    public String getVehicleStatus() {
        return this.vehicleStatus;
    }

}
