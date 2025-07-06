package com.matheus.VehicleManager.service;

import com.matheus.VehicleManager.enums.VehicleStatus;
import com.matheus.VehicleManager.model.Financing;
import com.matheus.VehicleManager.model.Vehicle;
import com.matheus.VehicleManager.repository.FinancingRepository;
import com.matheus.VehicleManager.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FinancingService {

    @Autowired
    private FinancingRepository financingRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    public boolean insert(Financing financing) {
        try {
            financingRepository.save(financing);
            Vehicle vehicle = financing.getVehicle();
            vehicle.setVehicleStatus(VehicleStatus.SOLD);
            vehicleRepository.save(vehicle);
            return true;
        } catch (Exception e) {
            System.err.println("Failed to insert financing");
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(Financing financing) {
        try {
            financingRepository.save(financing);

            Vehicle vehicle = financing.getVehicle();
            vehicle.setVehicleStatus(VehicleStatus.SOLD);
            vehicleRepository.save(vehicle);

            return true;
        } catch (Exception e) {
            System.err.println("Failed to insert financing");
            e.printStackTrace();
            return false;
        }
    }
}
