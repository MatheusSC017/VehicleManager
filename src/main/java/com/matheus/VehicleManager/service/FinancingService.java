package com.matheus.VehicleManager.service;

import com.matheus.VehicleManager.dto.FinancingRequestDTO;
import com.matheus.VehicleManager.enums.VehicleStatus;
import com.matheus.VehicleManager.exception.InvalidRequestException;
import com.matheus.VehicleManager.model.Client;
import com.matheus.VehicleManager.model.Financing;
import com.matheus.VehicleManager.model.Vehicle;
import com.matheus.VehicleManager.repository.ClientRepository;
import com.matheus.VehicleManager.repository.FinancingRepository;
import com.matheus.VehicleManager.repository.VehicleRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class FinancingService {

    @Autowired
    private FinancingRepository financingRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private ClientRepository clientRepository;

    public Page<Financing> getAll(int page, int size) {
        Pageable paging = PageRequest.of(page, size);
        return financingRepository.findAll(paging);
    }

    public Financing getById(Long financingId) {
        return financingRepository.getReferenceById(financingId);
    }

    @Transactional
    public Financing create(FinancingRequestDTO financingRequestDTO) throws IOException {
        Client client = clientRepository.findById(financingRequestDTO.getClient().getId()).orElse(null);
        Vehicle vehicle = vehicleRepository.findById(financingRequestDTO.getVehicle().id()).orElse(null);

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
        financing.setStatus(financingRequestDTO.getFinancingStatus());

        return financingRepository.save(financing);
    }


    @Transactional
    public Financing update(Long financingId, FinancingRequestDTO financingRequestDTO) throws IOException {
        Client client = clientRepository.findById(financingRequestDTO.getClient().getId()).orElse(null);
        Vehicle vehicle = vehicleRepository.findById(financingRequestDTO.getVehicle().id()).orElse(null);
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
        financing.setStatus(financingRequestDTO.getFinancingStatus());

        financingRepository.save(financing);

        vehicle.setVehicleStatus(VehicleStatus.SOLD);
        vehicleRepository.save(vehicle);

        return financing;
    }

    @Transactional
    public void delete(Long financingId) throws IOException {
        Financing financing = financingRepository.findById(financingId).orElse(null);
        if (financing != null) {
            Vehicle vehicle = financing.getVehicle();
            vehicle.setVehicleStatus(VehicleStatus.AVAILABLE);
            vehicleRepository.save(vehicle);

            financingRepository.delete(financing);
        }
        else {
            Map<String, String> errors = new HashMap<>();
            errors.put("financing", "Financiamento não encontrado");
            throw new InvalidRequestException(errors);
        }
    }

}
