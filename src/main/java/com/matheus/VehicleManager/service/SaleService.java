package com.matheus.VehicleManager.service;

import com.matheus.VehicleManager.dto.SaleRequestDTO;
import com.matheus.VehicleManager.enums.SalesStatus;
import com.matheus.VehicleManager.enums.VehicleStatus;
import com.matheus.VehicleManager.exception.InvalidRequestException;
import com.matheus.VehicleManager.model.Client;
import com.matheus.VehicleManager.model.Sale;
import com.matheus.VehicleManager.model.Vehicle;
import com.matheus.VehicleManager.repository.ClientRepository;
import com.matheus.VehicleManager.repository.SaleRepository;
import com.matheus.VehicleManager.repository.VehicleRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SaleService {

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private ClientRepository clientRepository;

    public Page<Sale> findAll(int page, int size) {
        Pageable paging = PageRequest.of(page, size);
        return saleRepository.findAll(paging);
    }

    public List<Sale> findAllByVehicleId(Long vehicleId) {
        return saleRepository.findByVehicleIdOrderByIdDesc(vehicleId);
    }

    public Sale findById(Long saleId) {
        return saleRepository.findById(saleId)
                .orElseThrow(() -> new EntityNotFoundException("Sale with id " + saleId + " not found"));
    }

    @Transactional
    public Sale create(SaleRequestDTO saleRequestDTO) {
        Client client = clientRepository.findById(saleRequestDTO.getClient()).orElse(null);
        Vehicle vehicle = vehicleRepository.findById(saleRequestDTO.getVehicle()).orElse(null);

        Map<String, String> errors = new HashMap<>();

        if (client == null) errors.put("client", "Cliente não encontrado");
        if (vehicle == null) errors.put("vehicle", "Veículo não encontrado");
        else if (vehicle.getVehicleStatus() != VehicleStatus.AVAILABLE) errors.put("vehicle", "Veículo não disponível");

        if (!errors.isEmpty()) throw new InvalidRequestException(errors);

        Sale sale = new Sale();
        sale.setClient(client);
        sale.setVehicle(vehicle);
        sale.setStatus(saleRequestDTO.getStatus());
        return saveSale(sale);
    }

    @Transactional
    public Sale update(Long saleId, SaleRequestDTO saleRequestDTO) {
        Map<String, String> errors = new HashMap<>();

        Client client = clientRepository.findById(saleRequestDTO.getClient()).orElse(null);
        Vehicle vehicle = vehicleRepository.findById(saleRequestDTO.getVehicle()).orElse(null);
        Sale sale = saleRepository.findById(saleId).orElse(null);

        if (client == null) errors.put("client", "Cliente não encontrado");
        if (vehicle == null) errors.put("vehicle", "Veículo não encontrado");
        if (sale == null) errors.put("sale", "Venda não encontrada");
        else if (!isValidStatusTransition(sale.getStatus(), saleRequestDTO.getStatus())) {
            errors.put("saleStatus", "Transição de status inválida: " + sale.getStatus() + " -> " + saleRequestDTO.getStatus());
        }

        if (vehicle != null && sale != null && !sale.getVehicle().getId().equals(vehicle.getId())) {
            if (vehicle.getVehicleStatus() != VehicleStatus.AVAILABLE) {
                errors.put("vehicle", "Veículo não disponível");
            } else {
                Vehicle currentVehicle = sale.getVehicle();
                currentVehicle.setVehicleStatus(VehicleStatus.AVAILABLE);
                vehicleRepository.save(currentVehicle);
            }
        }

        if (!errors.isEmpty()) throw new InvalidRequestException(errors);

        sale.setClient(client);
        sale.setVehicle(vehicle);
        sale.setStatus(saleRequestDTO.getStatus());

        return saveSale(sale);
    }

    private Sale saveSale(Sale sale) {
        Vehicle vehicle = sale.getVehicle();
        if (sale.getStatus() == SalesStatus.SOLD) {
            vehicle.setVehicleStatus(VehicleStatus.SOLD);
        } else if (sale.getStatus() == SalesStatus.RESERVED) {
            vehicle.setVehicleStatus(VehicleStatus.RESERVED);
        } else {
            vehicle.setVehicleStatus(VehicleStatus.AVAILABLE);
        }
        vehicleRepository.save(vehicle);
        return saleRepository.save(sale);
    }

    private boolean isValidStatusTransition(SalesStatus oldStatus, SalesStatus newStatus) {
        if (oldStatus == newStatus) return true;
        return (oldStatus == SalesStatus.RESERVED && (newStatus == SalesStatus.SOLD || newStatus == SalesStatus.CANCELED)) ||
                (oldStatus == SalesStatus.SOLD && newStatus == SalesStatus.CANCELED);
    }

}
