package com.matheus.VehicleManager.enums;

public enum FileType {

    IMAGE("Imagem"),
    FILE("Arquivo");

    private String fileType;

    private FileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFileType() {
        return this.fileType;
    }

}
