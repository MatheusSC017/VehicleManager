package com.matheus.VehicleManager.model;

import com.matheus.VehicleManager.enums.SalesStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Entity
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Cliente  é obrigatório")
    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    @NotNull(message = "Veiculo é obrigatório")
    @ManyToOne
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    private LocalDate salesDate;

    private LocalDate reserveDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    private SalesStatus status;

    private LocalDate createdAt;
    private LocalDate updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public @NotNull(message = "Cliente  é obrigatório") Client getClient() {
        return client;
    }

    public void setClient(@NotNull(message = "Cliente  é obrigatório") Client client) {
        this.client = client;
    }

    public @NotNull(message = "Veiculo é obrigatório") Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(@NotNull(message = "Veiculo é obrigatório") Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public @NotNull(message = "Data da venda") LocalDate getSalesDate() {
        return salesDate;
    }

    public void setSalesDate(@NotNull(message = "Data da venda") LocalDate salesDate) {
        this.salesDate = salesDate;
    }

    public @NotNull(message = "Data da reserva") LocalDate getReserveDate() {
        return reserveDate;
    }

    public void setReserveDate(@NotNull(message = "Data da reserva") LocalDate reserveDate) {
        this.reserveDate = reserveDate;
    }

    public @NotNull SalesStatus getStatus() {
        return status;
    }

    public void setStatus(@NotNull SalesStatus status) {
        this.status = status;
    }
}
