package com.matheus.VehicleManager.enums;

public enum VehicleFuel {

    GASOLINE("Gasolina"),
    ALCOHOL("Álcool"),
    FLEX("Flex"),
    DIESEL("Diesel"),
    HYBRID("Híbrido"),
    ELECTRIC("Elétrico");

    private String vehicleFuel;

    private VehicleFuel(String vehicleFuel) {
        this.vehicleFuel = vehicleFuel;
    }

    public String getVehicleFuel() {
        return this.vehicleFuel;
    }

}
