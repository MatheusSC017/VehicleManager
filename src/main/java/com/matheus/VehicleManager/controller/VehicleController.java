package com.matheus.VehicleManager.controller;

import com.matheus.VehicleManager.dto.VehicleWithOneImageDTO;
import com.matheus.VehicleManager.enums.FileType;
import com.matheus.VehicleManager.model.FileStore;
import com.matheus.VehicleManager.model.Vehicle;
import com.matheus.VehicleManager.repository.FileRepository;
import com.matheus.VehicleManager.repository.VehicleRepository;
import com.matheus.VehicleManager.service.FileStorageService;
import com.matheus.VehicleManager.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.List;

@Controller
public class VehicleController {

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping("/veiculos")
    public ModelAndView vehicles() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("vehicles/vehicles");
        modelAndView.addObject("vehiclesList", vehicleService.getVehiclesWithOneImage());
        return modelAndView;
    }

    @PostMapping("/veiculos/pesquisa")
    public ModelAndView searcVehicles(@RequestParam("searchInput") String search) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("vehicles/vehicles");
        List<VehicleWithOneImageDTO> filteredVehicles = vehicleService.getByBrandAndModelIgnoreCaseWithOneImage(search);
        modelAndView.addObject("vehiclesList", filteredVehicles);
        return modelAndView;
    }

    @GetMapping("/veiculos/filtro")
    public ModelAndView filterVehicles(@RequestParam("status") String status, @RequestParam("type") String type,
                                       @RequestParam("fuel") String fuel, @RequestParam("priceMin") int priceMin,
                                       @RequestParam("priceMax") int priceMax) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("vehicles/vehicles");
        List<VehicleWithOneImageDTO> vehiclesWithImage = vehicleService.getFilteredVehiclesWithOneImage(status, type, fuel, priceMin, priceMax);
        modelAndView.addObject("statusFilter", status);
        modelAndView.addObject("typeFilter", type);
        modelAndView.addObject("fuelFilter", fuel);
        modelAndView.addObject("priceMinFilter", priceMin);
        modelAndView.addObject("priceMaxFilter", priceMax);
        modelAndView.addObject("vehiclesList", vehiclesWithImage);
        return modelAndView;
    }

    @GetMapping("/veiculos/{id}")
    public ModelAndView vehicle(@PathVariable("id") Long id) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("vehicles/vehicle");
        VehicleWithOneImageDTO vehicle = vehicleService.getVehicleWithImageById(id);
        modelAndView.addObject("vehicleDTO", vehicle);
        return modelAndView;
    }

    @GetMapping("/veiculos/cadastrar")
    public ModelAndView getInsertForm(Vehicle vehicle) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("vehicles/vehicle_register");
        modelAndView.addObject("vehicle", new Vehicle());
        return modelAndView;
    }

    @PostMapping("/veiculos/cadastrar")
    public ModelAndView insert(@Valid Vehicle vehicle, BindingResult bindingResult, @RequestParam("imagesInput") MultipartFile[] images) {
        ModelAndView modelAndView = new ModelAndView();
        if (bindingResult.hasErrors()) {
            modelAndView.setViewName("vehicles/vehicle_register");
            modelAndView.addObject("vehicle", vehicle);
        } else {
            modelAndView.setViewName("redirect:/veiculos");
            vehicleRepository.save(vehicle);

            for (MultipartFile image : images) {
                if (image.isEmpty()) {
                    continue;
                }

                try {
                    String path = fileStorageService.storeFile(image);

                    FileStore imageEntity = new FileStore();
                    imageEntity.setFileName(image.getOriginalFilename());
                    imageEntity.setContentType(image.getContentType());
                    imageEntity.setPath(path);
                    imageEntity.setType(FileType.IMAGE);
                    imageEntity.setVehicle(vehicle);

                    fileRepository.save(imageEntity);
                } catch (IOException e) {
                    continue;
                }
            }

        }
        return modelAndView;
    }

    @GetMapping("/veiculos/{id}/editar")
    public ModelAndView getUpdateForm(@PathVariable("id") Long id) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("vehicles/vehicle_update");
        Vehicle vehicle = vehicleRepository.getReferenceById(id);
        modelAndView.addObject("vehicle", vehicle);
        return modelAndView;
    }

    @PostMapping("/veiculos/{id}/editar")
    public ModelAndView update(@Valid Vehicle vehicle, BindingResult bindingResult,
                               @RequestParam("imagesInput") MultipartFile[] images,
                               @RequestParam(value = "selectedImages", required = false) List<Long> selectedImageIds) {
        ModelAndView modelAndView = new ModelAndView();
        if (bindingResult.hasErrors()) {
            modelAndView.setViewName("vehicles/vehicle_update");
            modelAndView.addObject("vehicle", vehicle);
        } else {
            modelAndView.setViewName("redirect:/veiculos/" + vehicle.getId());
            vehicleRepository.save(vehicle);

            for (MultipartFile image : images) {
                if (image.isEmpty()) {
                    continue;
                }

                try {
                    String path = fileStorageService.storeFile(image);

                    FileStore imageEntity = new FileStore();
                    imageEntity.setFileName(image.getOriginalFilename());
                    imageEntity.setContentType(image.getContentType());
                    imageEntity.setPath(path);
                    imageEntity.setType(FileType.IMAGE);
                    imageEntity.setVehicle(vehicle);

                    fileRepository.save(imageEntity);
                } catch (IOException e) {
                    continue;
                }
            }

            if (selectedImageIds != null) {
                for (Long imageId : selectedImageIds) {
                    try {
                        fileRepository.deleteById(imageId);
                    } catch (Exception e) {
                        continue;
                    }
                }
            }
        }
        return modelAndView;
    }

    @GetMapping("/veiculos/{id}/deletar")
    public ModelAndView delete(@PathVariable("id") Long id) {
        try {
            vehicleRepository.deleteById(id);
        } catch (Exception e) {
            // pass
        }

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("redirect:/veiculos");
        return modelAndView;
    }

}
