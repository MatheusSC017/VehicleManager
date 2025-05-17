package com.matheus.VehicleManager.enums;


public enum VehicleType {

    CAR("Carro"),
    MOTORCYCLE("Moto");

    private String vehicleType;
    private VehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getVehicleType() {
        return vehicleType;
    }

}
