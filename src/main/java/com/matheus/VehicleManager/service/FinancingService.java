package com.matheus.VehicleManager.service;

import com.matheus.VehicleManager.dto.FinancingRequestDTO;
import com.matheus.VehicleManager.enums.FinancingStatus;
import com.matheus.VehicleManager.enums.VehicleStatus;
import com.matheus.VehicleManager.exception.InvalidRequestException;
import com.matheus.VehicleManager.model.Client;
import com.matheus.VehicleManager.model.Financing;
import com.matheus.VehicleManager.model.Vehicle;
import com.matheus.VehicleManager.repository.ClientRepository;
import com.matheus.VehicleManager.repository.FinancingRepository;
import com.matheus.VehicleManager.repository.VehicleRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class FinancingService {

    @Autowired
    private FinancingRepository financingRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Cacheable(value = "financings", key = "'page-' + #page + '-size-' + #size")
    public Page<Financing> getAll(int page, int size) {
        Pageable paging = PageRequest.of(page, size);
        return financingRepository.findAll(paging);
    }

    @Cacheable(value = "financings", key = "'id-' + #financingId")
    public Financing getById(Long financingId) {
        return financingRepository.findById(financingId)
                .orElseThrow(() -> new EntityNotFoundException("Financing with id " + financingId + " not found"));
    }

    @Cacheable(value = "financings", key = "'vehicle-' + #vehicleId")
    public Financing getByVehicleIdNotCanceled(Long vehicleId) {
        return financingRepository.findActiveByVehicleId(vehicleId)
                .orElseThrow(() -> new EntityNotFoundException("Financing with vehicle id " + vehicleId + " not found"));
    }

    @CacheEvict(value = "financings", allEntries = true)
    @Transactional
    public Financing create(FinancingRequestDTO financingRequestDTO) {
        Client client = clientRepository.findById(financingRequestDTO.getClient()).orElse(null);
        Vehicle vehicle = vehicleRepository.findById(financingRequestDTO.getVehicle()).orElse(null);

        Map<String, String> errors = new HashMap<>();
        if (client == null) errors.put("client", "Cliente não encontrado");
        if (vehicle == null) errors.put("vehicle", "Veículo não encontrado");
        else if (vehicle.getVehicleStatus() != VehicleStatus.AVAILABLE)
            errors.put("vehicle", "Veículo não disponível");

        if (!errors.isEmpty()) throw new InvalidRequestException(errors);

        vehicle.setVehicleStatus(VehicleStatus.SOLD);
        vehicleRepository.save(vehicle);

        Financing financing = new Financing();
        financing.setClient(client);
        financing.setVehicle(vehicle);
        financing.setTotalAmount(financingRequestDTO.getTotalAmount());
        financing.setDownPayment(financingRequestDTO.getDownPayment());
        financing.setInstallmentCount(financingRequestDTO.getInstallmentCount());
        financing.setInstallmentValue(financingRequestDTO.getInstallmentValue());
        financing.setAnnualInterestRate(financingRequestDTO.getAnnualInterestRate());
        financing.setContractDate(financingRequestDTO.getContractDate());
        financing.setFirstInstallmentDate(financingRequestDTO.getFirstInstallmentDate());

        return financingRepository.save(financing);
    }

    @CacheEvict(value = "financings", allEntries = true)
    @Transactional
    public Financing update(Long financingId, FinancingRequestDTO financingRequestDTO) {
        Client client = clientRepository.findById(financingRequestDTO.getClient()).orElse(null);
        Vehicle vehicle = vehicleRepository.findById(financingRequestDTO.getVehicle()).orElse(null);
        Financing financing = financingRepository.getReferenceById(financingId);

        Map<String, String> errors = new HashMap<>();

        if (client == null) errors.put("client", "Cliente não encontrado");
        if (vehicle == null) errors.put("vehicle", "Veículo não encontrado");
        else if (!financing.getVehicle().getId().equals(vehicle.getId())) {
            if (vehicle.getVehicleStatus() != VehicleStatus.AVAILABLE) {
                errors.put("vehicle", "Veículo não disponível");
            } else {
                Vehicle currentVehicle = financing.getVehicle();
                currentVehicle.setVehicleStatus(VehicleStatus.AVAILABLE);
                vehicleRepository.save(currentVehicle);
            }
        }
        if (!financing.getStatus().equals(FinancingStatus.DRAFT)) errors.put("financing", "Financiamento não pode ser alterado após fase de acolhimento");

        if (!errors.isEmpty()) throw new InvalidRequestException(errors);

        financing.setClient(client);
        financing.setVehicle(vehicle);
        financing.setTotalAmount(financingRequestDTO.getTotalAmount());
        financing.setDownPayment(financingRequestDTO.getDownPayment());
        financing.setInstallmentCount(financingRequestDTO.getInstallmentCount());
        financing.setInstallmentValue(financingRequestDTO.getInstallmentValue());
        financing.setAnnualInterestRate(financingRequestDTO.getAnnualInterestRate());
        financing.setContractDate(financingRequestDTO.getContractDate());
        financing.setFirstInstallmentDate(financingRequestDTO.getFirstInstallmentDate());

        financingRepository.save(financing);

        vehicle.setVehicleStatus(VehicleStatus.SOLD);
        vehicleRepository.save(vehicle);

        return financing;
    }

    @Transactional
    public void updateStatus(Long financingId, FinancingStatus status) {
        Financing financing = financingRepository.findById(financingId).orElse(null);

        Map<String, String> errors = new HashMap<>();
        if (financing == null) errors.put("financing", "Financiamento não encontrado");
        else if (!isValidStatusTransition(financing.getStatus(), status))
            errors.put("status", "Transição de status inválida: " + financing.getStatus() + " -> " + status);

        if (!errors.isEmpty()) throw new InvalidRequestException(errors);

        financing.setStatus(status);
        financingRepository.save(financing);

        if (financing.getStatus().equals(FinancingStatus.CANCELED)) {
            Vehicle vehicle = financing.getVehicle();
            vehicle.setVehicleStatus(VehicleStatus.AVAILABLE);
            vehicleRepository.save(vehicle);
        }
    }

    private boolean isValidStatusTransition(FinancingStatus oldStatus, FinancingStatus newStatus) {
        if (oldStatus == newStatus) return true;
        return (oldStatus == FinancingStatus.DRAFT && newStatus != FinancingStatus.DEFAULTED) ||
                (oldStatus == FinancingStatus.ACTIVE && newStatus != FinancingStatus.DRAFT);
    }

}
