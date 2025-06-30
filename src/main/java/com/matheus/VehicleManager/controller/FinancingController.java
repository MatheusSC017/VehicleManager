package com.matheus.VehicleManager.controller;

import com.matheus.VehicleManager.dto.FinancingResponseDTO;
import com.matheus.VehicleManager.model.Financing;
import com.matheus.VehicleManager.repository.FinancingRepository;
import com.matheus.VehicleManager.service.FinancingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/financing")
public class FinancingController {

    @Autowired
    private FinancingService financingService;

    @Autowired
    private FinancingRepository financingRepository;

    private FinancingResponseDTO toDTO(Financing financing) {
        return  new FinancingResponseDTO(
                financing.getId(),
                financing.getClient(),
                financing.getVehicle(),
                financing.getTotalAmount(),
                financing.getDownPayment(),
                financing.getInstallmentCount(),
                financing.getInstallmentValue(),
                financing.getDownPayment(),
                financing.getContractDate(),
                financing.getFirstInstallmentDate(),
                financing.getStatus()
        );
    }

    @GetMapping
    public ResponseEntity<List<FinancingResponseDTO>> financings() {
        List<Financing> financings = financingRepository.findAll();
        List<FinancingResponseDTO> financingsDtos = financings.stream()
                .map(this::toDTO)
                .toList();
        return ResponseEntity.ok(financingsDtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FinancingResponseDTO> financing(@PathVariable("id") Long financingId) {
        Financing financing = financingRepository.getReferenceById(financingId);
        return ResponseEntity.ok(toDTO(financing));
    }

    @PostMapping
    public  ResponseEntity<?> register(@Valid Financing financing, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, Object> response = new HashMap<>();
            response.put("content", toDTO(financing));

            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
            );
            response.put("errors", errors);

            return ResponseEntity.badRequest().body(response);
        }

        financingRepository.save(financing);

        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(financing));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@Valid Financing financing, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, Object> response = new HashMap<>();
            response.put("content", toDTO(financing));

            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
            );
            response.put("errors", errors);

            return ResponseEntity.badRequest().body(response);
        }

        financingRepository.save(financing);

        return ResponseEntity.ok(toDTO(financing));
    }

    @DeleteMapping("/id")
    public ResponseEntity<?> delete(@PathVariable Long financingId) {
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
