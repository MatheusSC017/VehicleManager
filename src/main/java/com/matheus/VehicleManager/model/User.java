package com.matheus.VehicleManager.model;

import com.matheus.VehicleManager.enums.UserRole;
import com.matheus.VehicleManager.validators.UniqueUsername;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    @UniqueUsername
    @Size(min = 1, max = 50, message = "O limite máximo de caracteres é 50")
    @NotNull(message = "Campo obrigatório")
    private String username;

    @Size(min = 1, max = 200, message = "O limite máximo de caracteres é 200")
    @NotNull(message = "Campo obrigatório")
    private String password;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Campo obrigatório")
    private UserRole role;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public @Size(min = 1, max = 200, message = "O limite máximo de caracteres é 200") String getPassword() {
        return password;
    }

    public void setPassword(@Size(min = 1, max = 200, message = "O limite máximo de caracteres é 200") String password) {
        this.password = password;
    }

    public @Size(min = 1, max = 50, message = "O limite máximo de caracteres é 50") String getUsername() {
        return username;
    }

    public void setUsername(@Size(min = 1, max = 50, message = "O limite máximo de caracteres é 50") String username) {
        this.username = username;
    }
}
