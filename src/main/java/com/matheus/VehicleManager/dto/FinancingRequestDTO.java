package com.matheus.VehicleManager.dto;

import com.matheus.VehicleManager.enums.FinancingStatus;

import com.matheus.VehicleManager.model.Client;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class FinancingRequestDTO {
    @NotNull(message = "Cliente é obrigatório")
    private Long client;

    @NotNull(message = "Veículo é obrigatório")
    private Long vehicle;

    @NotNull(message = "Valor total é obrigatório")
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal totalAmount;

    @NotNull(message = "Entrada é obrigatória")
    @DecimalMin(value = "0.0")
    private BigDecimal downPayment;

    @NotNull(message = "Número de parcelas é obrigatório")
    @Min(1)
    private Integer installmentCount;

    @NotNull(message = "Valor da parcela é obrigatório")
    @DecimalMin(value = "0.0")
    private BigDecimal installmentValue;

    @NotNull(message = "Taxa de juros anual é obrigatória")
    @DecimalMin(value = "0.0")
    private BigDecimal annualInterestRate;

    @NotNull(message = "Data do contrato é obrigatória")
    private LocalDate contractDate;

    @NotNull(message = "Data do primeiro pagamento é obrigatória")
    private LocalDate firstInstallmentDate;

    public @NotNull(message = "Cliente é obrigatório") Long getClient() {
        return client;
    }

    public void setClient(@NotNull(message = "Cliente é obrigatório") Long client) {
        this.client = client;
    }

    public @NotNull(message = "Veículo é obrigatório") Long getVehicle() {
        return vehicle;
    }

    public void setVehicle(@NotNull(message = "Veículo é obrigatório") Long vehicle) {
        this.vehicle = vehicle;
    }

    public @NotNull(message = "Valor total é obrigatório") @DecimalMin(value = "0.0", inclusive = false) BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(@NotNull(message = "Valor total é obrigatório") @DecimalMin(value = "0.0", inclusive = false) BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public @NotNull(message = "Entrada é obrigatória") @DecimalMin(value = "0.0") BigDecimal getDownPayment() {
        return downPayment;
    }

    public void setDownPayment(@NotNull(message = "Entrada é obrigatória") @DecimalMin(value = "0.0") BigDecimal downPayment) {
        this.downPayment = downPayment;
    }

    public @NotNull(message = "Número de parcelas é obrigatório") @Min(1) Integer getInstallmentCount() {
        return installmentCount;
    }

    public void setInstallmentCount(@NotNull(message = "Número de parcelas é obrigatório") @Min(1) Integer installmentCount) {
        this.installmentCount = installmentCount;
    }

    public @NotNull(message = "Valor da parcela é obrigatório") @DecimalMin(value = "0.0") BigDecimal getInstallmentValue() {
        return installmentValue;
    }

    public void setInstallmentValue(@NotNull(message = "Valor da parcela é obrigatório") @DecimalMin(value = "0.0") BigDecimal installmentValue) {
        this.installmentValue = installmentValue;
    }

    public @NotNull(message = "Taxa de juros anual é obrigatória") @DecimalMin(value = "0.0") BigDecimal getAnnualInterestRate() {
        return annualInterestRate;
    }

    public void setAnnualInterestRate(@NotNull(message = "Taxa de juros anual é obrigatória") @DecimalMin(value = "0.0") BigDecimal annualInterestRate) {
        this.annualInterestRate = annualInterestRate;
    }

    public @NotNull(message = "Data do contrato é obrigatória") LocalDate getContractDate() {
        return contractDate;
    }

    public void setContractDate(@NotNull(message = "Data do contrato é obrigatória") LocalDate contractDate) {
        this.contractDate = contractDate;
    }

    public @NotNull(message = "Data do primeiro pagamento é obrigatória") LocalDate getFirstInstallmentDate() {
        return firstInstallmentDate;
    }

    public void setFirstInstallmentDate(@NotNull(message = "Data do primeiro pagamento é obrigatória") LocalDate firstInstallmentDate) {
        this.firstInstallmentDate = firstInstallmentDate;
    }

}