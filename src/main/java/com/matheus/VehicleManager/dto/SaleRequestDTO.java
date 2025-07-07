package com.matheus.VehicleManager.dto;

import com.matheus.VehicleManager.enums.SalesStatus;
import com.matheus.VehicleManager.model.Client;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class SaleRequestDTO {
    @NotNull(message = "Cliente  é obrigatório")
    private Client client;

    @NotNull(message = "Veiculo é obrigatório")
    private VehicleMinimalDTO vehicle;

    private LocalDate salesDate;

    private LocalDate reserveDate;

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

    public LocalDate getSalesDate() {
        return salesDate;
    }

    public void setSalesDate(LocalDate salesDate) {
        this.salesDate = salesDate;
    }

    public LocalDate getReserveDate() {
        return reserveDate;
    }

    public void setReserveDate(LocalDate reserveDate) {
        this.reserveDate = reserveDate;
    }

    public @NotNull SalesStatus getStatus() {
        return status;
    }

    public void setStatus(@NotNull SalesStatus status) {
        this.status = status;
    }
}
