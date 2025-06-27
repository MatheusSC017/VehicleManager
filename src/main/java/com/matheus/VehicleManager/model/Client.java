package com.matheus.VehicleManager.model;

import com.matheus.VehicleManager.validators.UniqueEmail;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@UniqueEmail
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(min = 2, max = 50, message = "O limite máximo de caracteres é 50")
    @NotNull(message = "Campo obrigatório")
    private String firstName;

    @Size(min = 2, max = 50, message = "O limite máximo de caracteres é 50")
    @NotNull(message = "Campo obrigatório")
    private String lastName;

    @NotBlank(message = "Email is mandatory")
    @Column(unique = true)
    @Email
    @Size(min = 5, max = 50, message = "O limite máximo de caracteres é 50")
    private String email;

    @Size(min = 10, max = 20, message = "O limite mínimo de caracteres é 10 e o máximo é 20")
    @NotNull(message = "Campo obrigatório")
    private String phone;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public @Size(min = 2, max = 50, message = "O limite máximo de caracteres é 50") String getFirstName() {
        return firstName;
    }

    public void setFirstName(@Size(min = 2, max = 50, message = "O limite máximo de caracteres é 50") String firstName) {
        this.firstName = firstName;
    }

    public @Size(min = 2, max = 50, message = "O limite máximo de caracteres é 50") String getLastName() {
        return lastName;
    }

    public void setLastName(@Size(min = 2, max = 50, message = "O limite máximo de caracteres é 50") String lastName) {
        this.lastName = lastName;
    }

    public @Email @Size(min = 5, max = 50, message = "O limite máximo de caracteres é 50") String getEmail() {
        return email;
    }

    public void setEmail(@Email @Size(min = 5, max = 50, message = "O limite máximo de caracteres é 50") String email) {
        this.email = email;
    }

    public @Size(min = 10, max = 20, message = "O limite mínimo de caracteres é 10 e o máximo é 20") String getPhone() {
        return phone;
    }

    public void setPhone(@Size(min = 10, max = 20, message = "O limite mínimo de caracteres é 10 e o máximo é 20") String phone) {
        this.phone = phone;
    }
}
