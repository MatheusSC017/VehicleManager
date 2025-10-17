package com.matheus.VehicleManager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class VehicleManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(VehicleManagerApplication.class, args);
	}

}
