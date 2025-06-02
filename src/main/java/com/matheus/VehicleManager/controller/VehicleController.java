package com.matheus.VehicleManager.controller;

import com.matheus.VehicleManager.dto.VehicleWithImagesDTO;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/veiculos")
public class VehicleController {

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping
    public ModelAndView vehicles(@RequestParam(value="searchInput") Optional<String> search,
                                 @RequestParam("status") Optional<String> status,
                                 @RequestParam("type") Optional<String> type,
                                 @RequestParam("fuel") Optional<String> fuel,
                                 @RequestParam(value="priceMin", defaultValue="0") int priceMin,
                                 @RequestParam(value="priceMax", defaultValue="0") int priceMax) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("vehicles/vehicles");
        List<VehicleWithOneImageDTO> vehicles;
        vehicles = vehicleService.getFilteredVehiclesWithOneImage(search.orElse(""), status.orElse(""),
                type.orElse(""), fuel.orElse(""), priceMin, priceMax);
        modelAndView.addObject("searchFilter", search.orElse(""));
        modelAndView.addObject("statusFilter", status.orElse(""));
        modelAndView.addObject("typeFilter", type.orElse(""));
        modelAndView.addObject("fuelFilter", fuel.orElse(""));
        modelAndView.addObject("priceMinFilter", priceMin);
        modelAndView.addObject("priceMaxFilter", priceMax);
        modelAndView.addObject("vehiclesList", vehicles);
        return modelAndView;
    }

    @GetMapping("/{id}")
    public ModelAndView vehicle(@PathVariable("id") Long id) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("vehicles/vehicle");
        VehicleWithImagesDTO vehicle = vehicleService.getVehicleWithImagesById(id);
        modelAndView.addObject("vehicleDTO", vehicle);
        return modelAndView;
    }

    @GetMapping("/cadastrar")
    public ModelAndView getInsertForm(Vehicle vehicle) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("vehicles/vehicle_register");
        modelAndView.addObject("vehicle", new Vehicle());
        return modelAndView;
    }

    @PostMapping("/cadastrar")
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
                    System.err.println("Failed to store image: " + image.getOriginalFilename());
                    e.printStackTrace();
                    continue;
                }
            }
        }
        return modelAndView;
    }

    @GetMapping("/{id}/editar")
    public ModelAndView getUpdateForm(@PathVariable("id") Long id) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("vehicles/vehicle_update");
        Vehicle vehicle = vehicleRepository.getReferenceById(id);
        modelAndView.addObject("vehicle", vehicle);
        return modelAndView;
    }

    @PostMapping("/{id}/editar")
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
                    System.err.println("Failed to store image: " + image.getOriginalFilename());
                    e.printStackTrace();
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

    @GetMapping("/{id}/deletar")
    public ModelAndView delete(@PathVariable("id") Long id) {
        try {
            vehicleRepository.deleteById(id);
        } catch (Exception e) {
            System.err.println("Failed to delete vehicle: ");
            e.printStackTrace();
        }

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("redirect:/veiculos");
        return modelAndView;
    }

}
