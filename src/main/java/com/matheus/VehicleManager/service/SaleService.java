package com.matheus.VehicleManager.service;

import com.matheus.VehicleManager.enums.SalesStatus;
import com.matheus.VehicleManager.enums.VehicleStatus;
import com.matheus.VehicleManager.model.Financing;
import com.matheus.VehicleManager.model.Sale;
import com.matheus.VehicleManager.model.Vehicle;
import com.matheus.VehicleManager.repository.SaleRepository;
import com.matheus.VehicleManager.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SaleService {

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    public boolean insert(Sale sale) {
        return saveSale(sale);
    }

    public boolean update(Sale sale) {
        return saveSale(sale);
    }

    private boolean saveSale(Sale sale) {
        try {
            if (sale.getStatus() == SalesStatus.CANCELED) return false;
            saleRepository.save(sale);
            Vehicle vehicle = sale.getVehicle();
            if (sale.getStatus() == SalesStatus.SOLD) {
                vehicle.setVehicleStatus(VehicleStatus.SOLD);
            } else {
                vehicle.setVehicleStatus(VehicleStatus.RESERVED);
            }
            return true;
        } catch (Exception e) {
            System.err.println("Failed to insert financing");
            e.printStackTrace();
            return false;
        }
    }

}
