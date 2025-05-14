package com.matheus.VehicleManager.model;

import com.matheus.VehicleManager.enums.VehicleChange;
import com.matheus.VehicleManager.enums.VehicleFuel;
import com.matheus.VehicleManager.enums.VehicleStatus;
import com.matheus.VehicleManager.enums.VehicleType;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Entity
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_type")
    @Enumerated(EnumType.STRING)
    private VehicleType vehicleType;

    @Column(name = "vehicle_status")
    @Enumerated(EnumType.STRING)
    private VehicleStatus vehicleStatus;

    @Column(name = "model")
    @NotBlank(message = "O modelo não poder estar vazia")
    @Size(min = 1, max = 100, message = "O limite máximo de caracteres é 100")
    private String model;

    @Column(name = "brand")
    @NotBlank(message = "A marca não poder estar vazia")
    @Size(min = 1, max = 100, message = "O limite máximo de caracteres é 100")
    private String brand;

    @Column(name = "year")
    @Min(value = 1886, message = "Ano inválido")
    private int year;

    @Column(name = "color")
    @NotBlank(message = "A cor do veiculo não pode estar vazia")
    private String color;

    @Column(name = "engine_number")
    @NotBlank(message = "O número do motor não pode estar vazio")
    private String engineNumber;

    @Column(name = "plate")
    private String plate;

    @Column(name = "chassi")
    private String chassi;

    @Column(name = "mileage", precision = 10, scale = 1)
    @NotNull(message = "A quilometragem não pode ser nula")
    @DecimalMin(value = "0.00", inclusive = false, message = "A quilometragem deve ser maior que 0")
    private BigDecimal mileage;

    @Column(name = "price", precision = 10, scale = 2)
    @NotNull(message = "O preço não pode ser nulo")
    @DecimalMin(value = "0.00", inclusive = false, message = "O preço deve ser maior que 0")
    private BigDecimal price;

    @Column(name = "vehicle_fuel")
    @Enumerated(EnumType.STRING)
    private VehicleFuel vehicleFuel;

    @Column(name = "vehicle_change")
    @Enumerated(EnumType.STRING)
    private VehicleChange vehicleChange;

    @Column(name = "doors")
    @Min(value = 1, message = "Número de portas inválido")
    @Max(value = 4, message = "Número de portas inválido")
    private int doors;

    @Column(name = "motor")
    private String motor;

    @Column(name = "power")
    private String power;

}
