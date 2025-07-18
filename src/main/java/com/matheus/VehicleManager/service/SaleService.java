package com.matheus.VehicleManager.service;

import com.matheus.VehicleManager.dto.SaleRequestDTO;
import com.matheus.VehicleManager.enums.SalesStatus;
import com.matheus.VehicleManager.enums.VehicleStatus;
import com.matheus.VehicleManager.exception.InvalidStatusTransitionException;
import com.matheus.VehicleManager.exception.SaleNotFoundException;
import com.matheus.VehicleManager.exception.VehicleUnavailableException;
import com.matheus.VehicleManager.model.Sale;
import com.matheus.VehicleManager.model.Vehicle;
import com.matheus.VehicleManager.repository.SaleRepository;
import com.matheus.VehicleManager.repository.VehicleRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SaleService {

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Transactional
    public Sale insert(SaleRequestDTO saleRequestDTO) {
        Vehicle newVehicle = vehicleRepository.findById(saleRequestDTO.getVehicle().id())
                .orElseThrow(() -> new VehicleUnavailableException("Veículo não encontrado"));

        Sale sale = new Sale();
        sale.setClient(saleRequestDTO.getClient());
        sale.setVehicle(newVehicle);
        sale.setStatus(saleRequestDTO.getStatus());
        saveSale(sale);
        return sale;
    }

    @Transactional
    public Sale update(Long saleId, SaleRequestDTO saleRequestDTO) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new SaleNotFoundException("Venda de ID " + saleId + " não encontrada"));
        Vehicle newVehicle = vehicleRepository.findById(saleRequestDTO.getVehicle().id())
                .orElseThrow(() -> new VehicleUnavailableException("Veículo não encontrado"));

        if (!sale.getVehicle().getId().equals(newVehicle.getId())) {
            if (newVehicle.getVehicleStatus() != VehicleStatus.AVAILABLE) {
                throw new VehicleUnavailableException("Veículo não disponível");
            }
            Vehicle vehicle = sale.getVehicle();
            vehicle.setVehicleStatus(VehicleStatus.AVAILABLE);
            vehicleRepository.save(vehicle);
        }

        if (!isValidStatusTransition(sale.getStatus(), saleRequestDTO.getStatus())) {
            throw new InvalidStatusTransitionException("Transição de status inválida: " + sale.getStatus() + " -> " + saleRequestDTO.getStatus());
        }

        sale.setClient(saleRequestDTO.getClient());
        sale.setVehicle(newVehicle);
        sale.setStatus(saleRequestDTO.getStatus());

        saveSale(sale);
        return sale;
    }

    private void saveSale(Sale sale) {
        saleRepository.save(sale);
        Vehicle vehicle = sale.getVehicle();
        if (sale.getStatus() == SalesStatus.SOLD) {
            vehicle.setVehicleStatus(VehicleStatus.SOLD);
        } else if (sale.getStatus() == SalesStatus.RESERVED) {
            vehicle.setVehicleStatus(VehicleStatus.RESERVED);
        } else {
            vehicle.setVehicleStatus(VehicleStatus.AVAILABLE);
        }
        vehicleRepository.save(vehicle);
    }

    private boolean isValidStatusTransition(SalesStatus oldStatus, SalesStatus newStatus) {
        if (oldStatus == newStatus) return true;
        return (oldStatus == SalesStatus.RESERVED && (newStatus == SalesStatus.SOLD || newStatus == SalesStatus.CANCELED)) ||
                (oldStatus == SalesStatus.SOLD && newStatus == SalesStatus.CANCELED);
    }


}
