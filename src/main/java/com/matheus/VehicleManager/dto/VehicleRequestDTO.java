package com.matheus.VehicleManager.dto;

import com.matheus.VehicleManager.enums.VehicleChange;
import com.matheus.VehicleManager.enums.VehicleFuel;
import com.matheus.VehicleManager.enums.VehicleType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class VehicleRequestDTO {
    @NotNull(message = "Campo obrigatório")
    private VehicleType vehicleType;

    @NotBlank(message = "O modelo não poder estar vazia")
    @Size(min = 1, max = 100, message = "O limite máximo de caracteres é 100")
    private String model;

    @NotBlank(message = "A marca não poder estar vazia")
    @Size(min = 1, max = 100, message = "O limite máximo de caracteres é 100")
    private String brand;

    @Min(value = 1886, message = "Ano inválido")
    private int year;

    @NotBlank(message = "A cor do veiculo não pode estar vazia")
    private String color;

    @Size(max = 8, message = "O limite máximo de caracteres é 8")
    private String plate;

    @NotBlank(message = "Chassi is mandatory")
    @Size(max = 50, message = "O limite máximo de caracteres é 50")
    private String chassi;

    @NotNull(message = "A quilometragem não pode ser nula")
    @DecimalMin(value = "0.00", inclusive = false, message = "A quilometragem deve ser maior que 0")
    private BigDecimal mileage;

    @NotNull(message = "O preço não pode ser nulo")
    @DecimalMin(value = "0.00", inclusive = false, message = "O preço deve ser maior que 0")
    private BigDecimal price;

    @NotNull(message = "Campo obrigatório")
    private VehicleFuel vehicleFuel;

    private VehicleChange vehicleChange;

    @Min(value = 0, message = "Número de portas inválido")
    @Max(value = 4, message = "Número de portas inválido")
    private int doors;

    @Size(max = 50, message = "O limite máximo de caracteres é 50")
    private String motor;

    @Size(max = 50, message = "O limite máximo de caracteres é 50")
    private String power;

    public @NotNull(message = "Campo obrigatório") VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(@NotNull(message = "Campo obrigatório") VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    public @NotBlank(message = "O modelo não poder estar vazia") @Size(min = 1, max = 100, message = "O limite máximo de caracteres é 100") String getModel() {
        return model;
    }

    public void setModel(@NotBlank(message = "O modelo não poder estar vazia") @Size(min = 1, max = 100, message = "O limite máximo de caracteres é 100") String model) {
        this.model = model;
    }

    public @NotBlank(message = "A marca não poder estar vazia") @Size(min = 1, max = 100, message = "O limite máximo de caracteres é 100") String getBrand() {
        return brand;
    }

    public void setBrand(@NotBlank(message = "A marca não poder estar vazia") @Size(min = 1, max = 100, message = "O limite máximo de caracteres é 100") String brand) {
        this.brand = brand;
    }

    @Min(value = 1886, message = "Ano inválido")
    public int getYear() {
        return year;
    }

    public void setYear(@Min(value = 1886, message = "Ano inválido") int year) {
        this.year = year;
    }

    public @NotBlank(message = "A cor do veiculo não pode estar vazia") String getColor() {
        return color;
    }

    public void setColor(@NotBlank(message = "A cor do veiculo não pode estar vazia") String color) {
        this.color = color;
    }

    public @Size(max = 8, message = "O limite máximo de caracteres é 8") String getPlate() {
        return plate;
    }

    public void setPlate(@Size(max = 8, message = "O limite máximo de caracteres é 8") String plate) {
        this.plate = plate;
    }

    public @NotBlank(message = "Chassi is mandatory") @Size(max = 50, message = "O limite máximo de caracteres é 50") String getChassi() {
        return chassi;
    }

    public void setChassi(@NotBlank(message = "Chassi is mandatory") @Size(max = 50, message = "O limite máximo de caracteres é 50") String chassi) {
        this.chassi = chassi;
    }

    public @NotNull(message = "A quilometragem não pode ser nula") @DecimalMin(value = "0.00", inclusive = false, message = "A quilometragem deve ser maior que 0") BigDecimal getMileage() {
        return mileage;
    }

    public void setMileage(@NotNull(message = "A quilometragem não pode ser nula") @DecimalMin(value = "0.00", inclusive = false, message = "A quilometragem deve ser maior que 0") BigDecimal mileage) {
        this.mileage = mileage;
    }

    public @NotNull(message = "O preço não pode ser nulo") @DecimalMin(value = "0.00", inclusive = false, message = "O preço deve ser maior que 0") BigDecimal getPrice() {
        return price;
    }

    public void setPrice(@NotNull(message = "O preço não pode ser nulo") @DecimalMin(value = "0.00", inclusive = false, message = "O preço deve ser maior que 0") BigDecimal price) {
        this.price = price;
    }

    public @NotNull(message = "Campo obrigatório") VehicleFuel getVehicleFuel() {
        return vehicleFuel;
    }

    public void setVehicleFuel(@NotNull(message = "Campo obrigatório") VehicleFuel vehicleFuel) {
        this.vehicleFuel = vehicleFuel;
    }

    public VehicleChange getVehicleChange() {
        return vehicleChange;
    }

    public void setVehicleChange(VehicleChange vehicleChange) {
        this.vehicleChange = vehicleChange;
    }

    @Min(value = 0, message = "Número de portas inválido")
    @Max(value = 4, message = "Número de portas inválido")
    public int getDoors() {
        return doors;
    }

    public void setDoors(@Min(value = 0, message = "Número de portas inválido") @Max(value = 4, message = "Número de portas inválido") int doors) {
        this.doors = doors;
    }

    public @Size(max = 50, message = "O limite máximo de caracteres é 50") String getMotor() {
        return motor;
    }

    public void setMotor(@Size(max = 50, message = "O limite máximo de caracteres é 50") String motor) {
        this.motor = motor;
    }

    public @Size(max = 50, message = "O limite máximo de caracteres é 50") String getPower() {
        return power;
    }

    public void setPower(@Size(max = 50, message = "O limite máximo de caracteres é 50") String power) {
        this.power = power;
    }
}
