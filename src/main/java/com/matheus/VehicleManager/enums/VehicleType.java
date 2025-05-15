package com.matheus.VehicleManager.enums;


public enum VehicleType {

    CAR("Carro"),
    MOTORCYLCLE("Moto");

    private String vehicleType;
    private VehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getVehicleType() {
        return vehicleType;
    }

}
