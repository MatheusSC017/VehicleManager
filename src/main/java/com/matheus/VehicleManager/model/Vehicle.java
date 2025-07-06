package com.matheus.VehicleManager.model;

import com.matheus.VehicleManager.enums.VehicleChange;
import com.matheus.VehicleManager.enums.VehicleFuel;
import com.matheus.VehicleManager.enums.VehicleStatus;
import com.matheus.VehicleManager.enums.VehicleType;

import com.matheus.VehicleManager.validators.UniqueChassi;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@UniqueChassi
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Campo obrigatório")
    @Enumerated(EnumType.STRING)
    private VehicleType vehicleType;

    @Enumerated(EnumType.STRING)
    private VehicleStatus vehicleStatus;

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
    @Column(unique = true)
    @Size(max = 50, message = "O limite máximo de caracteres é 50")
    private String chassi;

    @Column(precision = 10, scale = 1)
    @NotNull(message = "A quilometragem não pode ser nula")
    @DecimalMin(value = "0.00", inclusive = false, message = "A quilometragem deve ser maior que 0")
    private BigDecimal mileage;

    @Column(precision = 10, scale = 2)
    @NotNull(message = "O preço não pode ser nulo")
    @DecimalMin(value = "0.00", inclusive = false, message = "O preço deve ser maior que 0")
    private BigDecimal price;

    @NotNull(message = "Campo obrigatório")
    @Enumerated(EnumType.STRING)
    private VehicleFuel vehicleFuel;

    @Enumerated(EnumType.STRING)
    private VehicleChange vehicleChange;

    @Min(value = 0, message = "Número de portas inválido")
    @Max(value = 4, message = "Número de portas inválido")
    private int doors;

    @Size(max = 50, message = "O limite máximo de caracteres é 50")
    private String motor;

    @Size(max = 50, message = "O limite máximo de caracteres é 50")
    private String power;

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FileStore> images;

    private LocalDate createdAt;
    private LocalDate updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDate.now();
        this.updatedAt = LocalDate.now();
        this.vehicleStatus = VehicleStatus.AVAILABLE;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDate.now();
    }

    public Long getId() {
        return id;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public VehicleStatus getVehicleStatus() {
        return vehicleStatus;
    }

    public @NotBlank(message = "O modelo não poder estar vazia") @Size(min = 1, max = 100, message = "O limite máximo de caracteres é 100") String getModel() {
        return model;
    }

    public @NotBlank(message = "A marca não poder estar vazia") @Size(min = 1, max = 100, message = "O limite máximo de caracteres é 100") String getBrand() {
        return brand;
    }

    @Min(value = 1886, message = "Ano inválido")
    public int getYear() {
        return year;
    }

    public @NotBlank(message = "A cor do veiculo não pode estar vazia") String getColor() {
        return color;
    }

    public @Size(max = 8, message = "O limite máximo de caracteres é 8") String getPlate() {
        return plate;
    }

    public @Size(max = 50, message = "O limite máximo de caracteres é 50") String getChassi() {
        return chassi;
    }

    public @NotNull(message = "A quilometragem não pode ser nula") @DecimalMin(value = "0.00", inclusive = false, message = "A quilometragem deve ser maior que 0") BigDecimal getMileage() {
        return mileage;
    }

    public @NotNull(message = "O preço não pode ser nulo") @DecimalMin(value = "0.00", inclusive = false, message = "O preço deve ser maior que 0") BigDecimal getPrice() {
        return price;
    }

    public VehicleFuel getVehicleFuel() {
        return vehicleFuel;
    }

    public VehicleChange getVehicleChange() {
        return vehicleChange;
    }

    @Min(value = 0, message = "Número de portas inválido")
    @Max(value = 4, message = "Número de portas inválido")
    public int getDoors() {
        return doors;
    }

    public @Size(max = 50, message = "O limite máximo de caracteres é 50") String getMotor() {
        return motor;
    }

    public @Size(max = 50, message = "O limite máximo de caracteres é 50") String getPower() {
        return power;
    }

    public List<FileStore> getImages() {
        return images;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    public void setVehicleStatus(VehicleStatus vehicleStatus) {
        this.vehicleStatus = vehicleStatus;
    }

    public void setModel(@NotBlank(message = "O modelo não poder estar vazia") @Size(min = 1, max = 100, message = "O limite máximo de caracteres é 100") String model) {
        this.model = model;
    }

    public void setBrand(@NotBlank(message = "A marca não poder estar vazia") @Size(min = 1, max = 100, message = "O limite máximo de caracteres é 100") String brand) {
        this.brand = brand;
    }

    public void setYear(@Min(value = 1886, message = "Ano inválido") int year) {
        this.year = year;
    }

    public void setColor(@NotBlank(message = "A cor do veiculo não pode estar vazia") String color) {
        this.color = color;
    }

    public void setPlate(@Size(max = 8, message = "O limite máximo de caracteres é 8") String plate) {
        this.plate = plate;
    }

    public void setChassi(@Size(max = 50, message = "O limite máximo de caracteres é 50") String chassi) {
        this.chassi = chassi;
    }

    public void setMileage(@NotNull(message = "A quilometragem não pode ser nula") @DecimalMin(value = "0.00", inclusive = false, message = "A quilometragem deve ser maior que 0") BigDecimal mileage) {
        this.mileage = mileage;
    }

    public void setPrice(@NotNull(message = "O preço não pode ser nulo") @DecimalMin(value = "0.00", inclusive = false, message = "O preço deve ser maior que 0") BigDecimal price) {
        this.price = price;
    }

    public void setVehicleFuel(VehicleFuel vehicleFuel) {
        this.vehicleFuel = vehicleFuel;
    }

    public void setVehicleChange(VehicleChange vehicleChange) {
        this.vehicleChange = vehicleChange;
    }

    public void setDoors(@Min(value = 0, message = "Número de portas inválido") @Max(value = 4, message = "Número de portas inválido") int doors) {
        this.doors = doors;
    }

    public void setMotor(@Size(max = 50, message = "O limite máximo de caracteres é 50") String motor) {
        this.motor = motor;
    }

    public void setPower(@Size(max = 50, message = "O limite máximo de caracteres é 50") String power) {
        this.power = power;
    }

    public void setImages(List<FileStore> images) {
        this.images = images;
    }
}
