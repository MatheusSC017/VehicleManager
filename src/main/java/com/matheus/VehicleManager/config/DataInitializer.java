package com.matheus.VehicleManager.config;

import com.matheus.VehicleManager.enums.UserRole;
import com.matheus.VehicleManager.model.User;
import com.matheus.VehicleManager.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByUsername("admin") == null) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("Admin5432"));
                admin.setRole(UserRole.ADMIN);
                userRepository.save(admin);
                System.out.println("Admin user created");
            }
        };
    }

}
