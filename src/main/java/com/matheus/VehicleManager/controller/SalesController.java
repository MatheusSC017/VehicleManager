package com.matheus.VehicleManager.controller;

import com.matheus.VehicleManager.dto.*;
import com.matheus.VehicleManager.exception.InvalidRequestException;
import com.matheus.VehicleManager.model.Sale;
import com.matheus.VehicleManager.model.Vehicle;
import com.matheus.VehicleManager.service.SaleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sales")
public class SalesController {

    @Autowired
    private SaleService saleService;

    private SaleResponseDTO toDTO(Sale sales) {
        VehicleMinimalDTO vehicleDTO = new VehicleMinimalDTO(
                sales.getVehicle().getId(),
                sales.getVehicle().getChassi(),
                sales.getVehicle().getBrand(),
                sales.getVehicle().getModel()
        );

        return new SaleResponseDTO(
            sales.getId(),
            sales.getClient(),
            vehicleDTO,
            sales.getSalesDate(),
            sales.getReserveDate(),
            sales.getStatus()
        );
    }

    @GetMapping
    public ResponseEntity<Page<SaleResponseDTO>> getAll(@RequestParam(value = "page", defaultValue = "0") int page,
                                                        @RequestParam(value = "size", defaultValue = "10") int size) {
        Page<Sale> sales = saleService.findAll(page, size);
        Page<SaleResponseDTO> salesDtoPage = sales.map(this::toDTO);
        return ResponseEntity.ok(salesDtoPage);
    }

    @GetMapping("/vehicle/{id}")
    public ResponseEntity<List<SaleResponseDTO>> getAllByVehicle(@PathVariable("id") Long vehicleId) {
        List<Sale> sales = saleService.findAllByVehicleId(vehicleId);
        List<SaleResponseDTO> salesDtoPage = sales.stream().map(this::toDTO).toList();
        return ResponseEntity.ok(salesDtoPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable("id") Long saleId) {
        try {
            Sale sale = saleService.findById(saleId);
            return ResponseEntity.ok(toDTO(sale));
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            response.put("errors", errors);
            response.put("content", "");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping
    public  ResponseEntity<?> insert(@Valid @RequestBody SaleRequestDTO saleRequestDTO, BindingResult bindingResult) {
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
            Sale sale = saleService.create(saleRequestDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(sale));
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
    public ResponseEntity<?> update(@PathVariable("id") Long saleId, @Valid @RequestBody SaleRequestDTO saleRequestDTO, BindingResult bindingResult) {
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
            Sale sale = saleService.update(saleId, saleRequestDTO);
            return ResponseEntity.status(HttpStatus.OK).body(toDTO(sale));
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

}
