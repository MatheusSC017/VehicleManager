package com.matheus.VehicleManager.dto;

import com.matheus.VehicleManager.enums.FinancingStatus;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class FinancingRequestDTO {
    @NotNull(message = "Cliente é obrigatório")
    public Long client;

    @NotNull(message = "Veículo é obrigatório")
    public Long vehicle;

    @NotNull(message = "Valor total é obrigatório")
    @DecimalMin(value = "0.0", inclusive = false)
    public BigDecimal totalAmount;

    @NotNull(message = "Entrada é obrigatória")
    @DecimalMin(value = "0.0")
    public BigDecimal downPayment;

    @NotNull(message = "Número de parcelas é obrigatório")
    @Min(1)
    public Integer installmentCount;

    @NotNull(message = "Valor da parcela é obrigatório")
    @DecimalMin(value = "0.0")
    public BigDecimal installmentValue;

    @NotNull(message = "Taxa de juros anual é obrigatória")
    @DecimalMin(value = "0.0")
    public BigDecimal annualInterestRate;

    @NotNull(message = "Data do contrato é obrigatória")
    public LocalDate contractDate;

    @NotNull(message = "Data do primeiro pagamento é obrigatória")
    public LocalDate firstInstallmentDate;

    @NotNull(message = "Status do financiamento é obrigatório")
    public FinancingStatus financingStatus;
}