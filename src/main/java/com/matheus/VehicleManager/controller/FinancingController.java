package com.matheus.VehicleManager.controller;

import com.matheus.VehicleManager.dto.FinancingRequestDTO;
import com.matheus.VehicleManager.dto.FinancingResponseDTO;
import com.matheus.VehicleManager.dto.VehicleMinimalDTO;
import com.matheus.VehicleManager.exception.InvalidRequestException;
import com.matheus.VehicleManager.model.Financing;
import com.matheus.VehicleManager.service.FinancingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/financings")
public class FinancingController {

    @Autowired
    private FinancingService financingService;

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
        Page<Financing> financings = financingService.getAll(page, size);
        Page<FinancingResponseDTO> financingsDtoPage = financings.map(this::toDTO);
        return ResponseEntity.ok(financingsDtoPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FinancingResponseDTO> get(@PathVariable("id") Long financingId) {
        Financing financing = financingService.getById(financingId);
        return ResponseEntity.ok(toDTO(financing));
    }

    @PostMapping
    public  ResponseEntity<?> insert(@Valid @RequestBody FinancingRequestDTO financingDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, Object> response = new HashMap<>();
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
            );
            response.put("errors", errors);
            response.put("content", "");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            Financing financing = financingService.create(financingDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(financing));
        } catch (InvalidRequestException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("errors", e.getFieldErrors());
            response.put("content", "");
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            Map<String, String> errors = new HashMap<>();
            errors.put("server", "Erro interno: " + e.getMessage());
            response.put("errors", errors);
            response.put("content", "");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable("id") Long financingId, @Valid @RequestBody FinancingRequestDTO financingDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, Object> response = new HashMap<>();
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
            );
            response.put("errors", errors);
            response.put("content", "");

            return ResponseEntity.badRequest().body(response);
        }

        try {
            Financing financing = financingService.update(financingId, financingDto);
            return ResponseEntity.ok(toDTO(financing));
        } catch (InvalidRequestException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("errors", e.getFieldErrors());
            response.put("content", "");
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            response.put("errors", errors);
            response.put("content", "");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long financingId) {
        try {
            financingService.delete(financingId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            response.put("errors", errors);
            response.put("content", "");
            return ResponseEntity.badRequest().body(response);
        }
    }

}
