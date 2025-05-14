package com.matheus.VehicleManager.enums;

public enum VehicleFuel {

    GASOLINE("Gasoline"),
    ALCOHOL("Alcohol"),
    FLEX("Flex"),
    DIESEL("Diesel"),
    HYBRID("Hybrid"),
    ELECTRIC("Electric");

    private String vehicleFuel;

    private VehicleFuel(String vehicleFuel) { this.vehicleFuel = vehicleFuel; }

}
