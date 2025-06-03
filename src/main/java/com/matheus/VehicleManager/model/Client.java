package com.matheus.VehicleManager.model;

import com.matheus.VehicleManager.validators.UniqueEmail;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

@Entity
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(min = 2, max = 50, message = "O limite máximo de caracteres é 50")
    private String first_name;

    @Size(min = 2, max = 50, message = "O limite máximo de caracteres é 50")
    private String last_name;

    @Column(unique = true)
    @UniqueEmail
    @Email
    @Size(min = 5, max = 50, message = "O limite máximo de caracteres é 50")
    private String email;

    @Size(min = 10, max = 20, message = "O limite mínimo de caracteres é 10 e o máximo é 20")
    private String phone;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public @Size(min = 2, max = 50, message = "O limite máximo de caracteres é 50") String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(@Size(min = 2, max = 50, message = "O limite máximo de caracteres é 50") String first_name) {
        this.first_name = first_name;
    }

    public @Size(min = 2, max = 50, message = "O limite máximo de caracteres é 50") String getLast_name() {
        return last_name;
    }

    public void setLast_name(@Size(min = 2, max = 50, message = "O limite máximo de caracteres é 50") String last_name) {
        this.last_name = last_name;
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
