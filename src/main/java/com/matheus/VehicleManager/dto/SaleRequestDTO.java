package com.matheus.VehicleManager.dto;

import com.matheus.VehicleManager.enums.SalesStatus;
import com.matheus.VehicleManager.model.Client;
import jakarta.validation.constraints.NotNull;

public class SaleRequestDTO {
    @NotNull(message = "Cliente  é obrigatório")
    private Client client;

    @NotNull(message = "Veiculo é obrigatório")
    private VehicleMinimalDTO vehicle;

    @NotNull
    private SalesStatus status;

    public @NotNull(message = "Cliente  é obrigatório") Client getClient() {
        return client;
    }

    public void setClient(@NotNull(message = "Cliente  é obrigatório") Client client) {
        this.client = client;
    }

    public @NotNull(message = "Veiculo é obrigatório") VehicleMinimalDTO getVehicle() {
        return vehicle;
    }

    public void setVehicle(@NotNull(message = "Veiculo é obrigatório") VehicleMinimalDTO vehicle) {
        this.vehicle = vehicle;
    }

    public @NotNull SalesStatus getStatus() {
        return status;
    }

    public void setStatus(@NotNull SalesStatus status) {
        this.status = status;
    }
}
