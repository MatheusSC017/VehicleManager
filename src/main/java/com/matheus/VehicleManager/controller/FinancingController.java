package com.matheus.VehicleManager.controller;

import com.matheus.VehicleManager.dto.FinancingRequestDTO;
import com.matheus.VehicleManager.dto.FinancingResponseDTO;
import com.matheus.VehicleManager.dto.VehicleMinimalDTO;
import com.matheus.VehicleManager.enums.VehicleStatus;
import com.matheus.VehicleManager.model.Client;
import com.matheus.VehicleManager.model.Financing;
import com.matheus.VehicleManager.model.Vehicle;
import com.matheus.VehicleManager.repository.ClientRepository;
import com.matheus.VehicleManager.repository.FinancingRepository;
import com.matheus.VehicleManager.repository.VehicleRepository;
import com.matheus.VehicleManager.service.FinancingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/financings")
public class FinancingController {

    @Autowired
    private FinancingService financingService;

    @Autowired
    private FinancingRepository financingRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    private FinancingResponseDTO toDTO(Financing financing) {
        VehicleMinimalDTO vehicleDTO = new VehicleMinimalDTO(
            financing.getVehicle().getId(),
            financing.getVehicle().getChassi(),
            financing.getVehicle().getBrand(),
            financing.getVehicle().getModel()
        );

        return  new FinancingResponseDTO(
            financing.getId(),
            financing.getClient(),
            vehicleDTO,
            financing.getTotalAmount(),
            financing.getDownPayment(),
            financing.getInstallmentCount(),
            financing.getInstallmentValue(),
            financing.getAnnualInterestRate(),
            financing.getContractDate(),
            financing.getFirstInstallmentDate(),
            financing.getStatus()
        );
    }

    @GetMapping
    public ResponseEntity<Page<FinancingResponseDTO>> getAll(@RequestParam(value = "page", defaultValue = "0") int page,
                                                             @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable paging = PageRequest.of(page, size);
        Page<Financing> financings = financingRepository.findAll(paging);
        Page<FinancingResponseDTO> financingsDtoPage = financings.map(this::toDTO);
        return ResponseEntity.ok(financingsDtoPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FinancingResponseDTO> get(@PathVariable("id") Long financingId) {
        Financing financing = financingRepository.getReferenceById(financingId);
        return ResponseEntity.ok(toDTO(financing));
    }

    @PostMapping
    public  ResponseEntity<?> insert(@Valid @RequestBody FinancingRequestDTO financingDto, BindingResult bindingResult) {
        Map<String, Object> response = new HashMap<>();

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
            );
            response.put("errors", errors);
            response.put("content", "");
            return ResponseEntity.badRequest().body(response);
        }

        Optional<Client> clientOpt = clientRepository.findById(financingDto.getClient().getId());
        Optional<Vehicle> vehicleOpt = vehicleRepository.findById(financingDto.getVehicle().id());

        if (clientOpt.isEmpty() || vehicleOpt.isEmpty() || vehicleOpt.get().getVehicleStatus() != VehicleStatus.AVAILABLE) {
            Map<String, String> errors = new HashMap<>();
            if (clientOpt.isEmpty()) errors.put("client", "Cliente não encontrado");
            if (vehicleOpt.isEmpty()) errors.put("vehicle", "Veículo não encontrado");
            if (vehicleOpt.get().getVehicleStatus() != VehicleStatus.AVAILABLE) errors.put("vehicle", "Veículo não disponível");
            response.put("errors", errors);
            response.put("content", "");
            return ResponseEntity.badRequest().body(response);
        }

        Financing financing = new Financing();
        financing.setClient(clientOpt.get());
        financing.setVehicle(vehicleOpt.get());
        financing.setTotalAmount(financingDto.getTotalAmount());
        financing.setDownPayment(financingDto.getDownPayment());
        financing.setInstallmentCount(financingDto.getInstallmentCount());
        financing.setInstallmentValue(financingDto.getInstallmentValue());
        financing.setAnnualInterestRate(financingDto.getAnnualInterestRate());
        financing.setContractDate(financingDto.getContractDate());
        financing.setFirstInstallmentDate(financingDto.getFirstInstallmentDate());
        financing.setStatus(financingDto.getFinancingStatus());

        boolean inserted = financingService.insert(financing);
        if (!inserted) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(financing));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable("id") Long financingId, @Valid @RequestBody FinancingRequestDTO financingDto, BindingResult bindingResult) {
        Map<String, Object> response = new HashMap<>();

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
            );
            response.put("errors", errors);
            response.put("content", "");

            return ResponseEntity.badRequest().body(response);
        }

        Financing financing = financingRepository.getReferenceById(financingId);

        Optional<Client> clientOpt = clientRepository.findById(financingDto.getClient().getId());
        Optional<Vehicle> vehicleOpt = vehicleRepository.findById(financingDto.getVehicle().id());

        if (clientOpt.isEmpty() || vehicleOpt.isEmpty()) {
            Map<String, String> errors = new HashMap<>();
            if (clientOpt.isEmpty()) errors.put("client", "Cliente não encontrado");
            if (vehicleOpt.isEmpty()) errors.put("vehicle", "Veículo não encontrado");
            response.put("errors", errors);
            response.put("content", "");
            return ResponseEntity.badRequest().body(response);
        }

        if (financing.getVehicle().getId() != vehicleOpt.get().getId()) {
            Vehicle vehicle = financing.getVehicle();
            vehicle.setVehicleStatus(VehicleStatus.AVAILABLE);
            vehicleRepository.save(vehicle);
            if (vehicleOpt.get().getVehicleStatus() != VehicleStatus.AVAILABLE) {
                Map<String, String> errors = new HashMap<>();
                errors.put("vehicle", "Veículo não disponível");
                return ResponseEntity.badRequest().body(response);
            }
        }

        financing.setClient(clientOpt.get());
        financing.setVehicle(vehicleOpt.get());
        financing.setTotalAmount(financingDto.getTotalAmount());
        financing.setDownPayment(financingDto.getDownPayment());
        financing.setInstallmentCount(financingDto.getInstallmentCount());
        financing.setInstallmentValue(financingDto.getInstallmentValue());
        financing.setAnnualInterestRate(financingDto.getAnnualInterestRate());
        financing.setContractDate(financingDto.getContractDate());
        financing.setFirstInstallmentDate(financingDto.getFirstInstallmentDate());
        financing.setStatus(financingDto.getFinancingStatus());

        boolean updated = financingService.update(financing);
        if (!updated) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        };

        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(financing));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long financingId) {
        try {
            financingRepository.deleteById(financingId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            System.err.println("Failed to delete Financing: ");
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
