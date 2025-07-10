package com.matheus.VehicleManager.controller;

import com.matheus.VehicleManager.dto.*;
import com.matheus.VehicleManager.enums.VehicleStatus;
import com.matheus.VehicleManager.model.Client;
import com.matheus.VehicleManager.model.Sale;
import com.matheus.VehicleManager.model.Vehicle;
import com.matheus.VehicleManager.repository.ClientRepository;
import com.matheus.VehicleManager.repository.SaleRepository;
import com.matheus.VehicleManager.repository.VehicleRepository;
import com.matheus.VehicleManager.service.SaleService;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/sales")
public class SalesController {

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private SaleService saleService;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

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
        Pageable paging = PageRequest.of(page, size);
        Page<Sale> sales = saleRepository.findAll(paging);
        Page<SaleResponseDTO> salesDtoPage = sales.map(this::toDTO);
        return ResponseEntity.ok(salesDtoPage);
    }

    @GetMapping("/vehicle/{id}")
    public ResponseEntity<List<SaleResponseDTO>> getAllByVehicle(@PathVariable("id") Long vehicleId) {
        List<Sale> sales = saleRepository.findByVehicleId(vehicleId);
        List<SaleResponseDTO> salesDtoPage = sales.stream().map(this::toDTO).toList();
        return ResponseEntity.ok(salesDtoPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SaleResponseDTO> get(@PathVariable("id") Long saleId) {
        Sale sale = saleRepository.getReferenceById(saleId);
        return ResponseEntity.ok(toDTO(sale));
    }

    @PostMapping
    public  ResponseEntity<?> insert(@Valid @RequestBody SaleRequestDTO saleRequestDTO, BindingResult bindingResult) {
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

        Optional<Client> clientOpt = clientRepository.findById(saleRequestDTO.getClient().getId());
        Optional<Vehicle> vehicleOpt = vehicleRepository.findById(saleRequestDTO.getVehicle().id());

        if (clientOpt.isEmpty() || vehicleOpt.isEmpty() || vehicleOpt.get().getVehicleStatus() != VehicleStatus.AVAILABLE) {
            Map<String, String> errors = new HashMap<>();
            if (clientOpt.isEmpty()) errors.put("client", "Cliente não encontrado");
            if (vehicleOpt.isEmpty()) errors.put("vehicle", "Veículo não encontrado");
            if (vehicleOpt.get().getVehicleStatus() != VehicleStatus.AVAILABLE) errors.put("vehicle", "Veículo não disponível");
            response.put("errors", errors);
            response.put("content", "");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            Sale sale = saleService.insert(saleRequestDTO);
            return ResponseEntity.status(HttpStatus.OK).body(toDTO(sale));
        } catch (Exception e) {
            response.put("errors", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable("id") Long saleId, @Valid @RequestBody SaleRequestDTO saleRequestDTO, BindingResult bindingResult) {
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

        Optional<Client> clientOpt = clientRepository.findById(saleRequestDTO.getClient().getId());
        Optional<Vehicle> vehicleOpt = vehicleRepository.findById(saleRequestDTO.getVehicle().id());

        if (clientOpt.isEmpty() || vehicleOpt.isEmpty()) {
            Map<String, String> errors = new HashMap<>();
            if (clientOpt.isEmpty()) errors.put("client", "Cliente não encontrado");
            if (vehicleOpt.isEmpty()) errors.put("vehicle", "Veículo não encontrado");
            response.put("errors", errors);
            response.put("content", "");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            Sale sale = saleService.update(saleId, saleRequestDTO);
            return ResponseEntity.status(HttpStatus.OK).body(toDTO(sale));
        } catch (Exception e) {
            response.put("errors", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}
