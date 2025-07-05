package com.matheus.VehicleManager.dto;

import com.matheus.VehicleManager.enums.FinancingStatus;
import com.matheus.VehicleManager.model.Client;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FinancingResponseDTO(
    Long id,
    Client client,
    VehicleMinimalDTO vehicle,
    BigDecimal totalAmount,
    BigDecimal downPayment,
    Integer installmentCount,
    BigDecimal installmentValue,
    BigDecimal annualInterestRate,
    LocalDate contractDate,
    LocalDate firstInstallmentDate,
    FinancingStatus financingStatus
) {
}
