package com.matheus.VehicleManager.controller;

import com.matheus.VehicleManager.dto.*;
import com.matheus.VehicleManager.model.Sale;
import com.matheus.VehicleManager.service.SaleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales")
public class SalesController {

    @Autowired
    private SaleService saleService;

    private static SaleResponseDTO toDTO(Sale sales) {
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
        Page<SaleResponseDTO> salesDtoPage = sales.map(SalesController::toDTO);
        return ResponseEntity.ok(salesDtoPage);
    }

    @GetMapping("/vehicle/{id}")
    public ResponseEntity<List<SaleResponseDTO>> getAllByVehicle(@PathVariable("id") Long vehicleId) {
        List<Sale> sales = saleService.findAllByVehicleId(vehicleId);
        List<SaleResponseDTO> salesDtoPage = sales.stream().map(SalesController::toDTO).toList();
        return ResponseEntity.ok(salesDtoPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable("id") Long saleId) {
        Sale sale = saleService.findById(saleId);
        return ResponseEntity.ok(toDTO(sale));
    }

    @PostMapping
    public  ResponseEntity<?> insert(@Valid @RequestBody SaleRequestDTO saleRequestDTO) {
        Sale sale = saleService.create(saleRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(sale));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable("id") Long saleId, @Valid @RequestBody SaleRequestDTO saleRequestDTO) {
        Sale sale = saleService.update(saleId, saleRequestDTO);
        return ResponseEntity.status(HttpStatus.OK).body(toDTO(sale));
    }

}
