package com.matheus.VehicleManager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class VehicleManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(VehicleManagerApplication.class, args);
	}

}
