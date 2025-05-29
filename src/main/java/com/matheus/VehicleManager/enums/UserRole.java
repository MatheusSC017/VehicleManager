package com.matheus.VehicleManager.enums;

public enum UserRole {

    ADMIN("Administrador"),
    USER("Usuário");

    private String userRole;

    private UserRole(String userRole) {
        this.userRole = userRole;
    }

    public String getUserRole() {
        return this.userRole;
    }

}
