package com.matheus.VehicleManager.dto;

import com.matheus.VehicleManager.enums.SalesStatus;
import jakarta.validation.constraints.NotNull;

public class SaleRequestDTO {
    @NotNull(message = "Cliente  é obrigatório")
    private Long client;

    @NotNull(message = "Veiculo é obrigatório")
    private Long vehicle;

    @NotNull
    private SalesStatus status;

    public @NotNull(message = "Cliente  é obrigatório") Long getClient() {
        return client;
    }

    public void setClient(@NotNull(message = "Cliente  é obrigatório") Long client) {
        this.client = client;
    }

    public @NotNull(message = "Veiculo é obrigatório") Long getVehicle() {
        return vehicle;
    }

    public void setVehicle(@NotNull(message = "Veiculo é obrigatório") Long vehicle) {
        this.vehicle = vehicle;
    }

    public @NotNull SalesStatus getStatus() {
        return status;
    }

    public void setStatus(@NotNull SalesStatus status) {
        this.status = status;
    }
}
