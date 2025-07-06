package com.matheus.VehicleManager.enums;

public enum VehicleStatus {

    AVAILABLE("Disponível"),
    RESERVED("Reservado"),
    SOLD("Vendido"),
    MAINTENANCE("Manutenção");

    private String vehicleStatus;

    private VehicleStatus(String vehicleStatus) {
        this.vehicleStatus = vehicleStatus;
    }

    public String getVehicleStatus() {
        return this.vehicleStatus;
    }

}
