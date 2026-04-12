package com.abcm0018.sai.users.domain.enums;

import lombok.Getter;

import java.util.List;

@Getter
public enum Status {
    ACTIVE("Activo"), //Activo
    INACTIVE("Inactivo"), //Inactivo
    BLOCKED("Bloqueado"), //Bloqueado
    EXPIRED("Expirado"); //Expirado

    private final String displayStatus;

    Status(String displayStatus) {
        this.displayStatus = displayStatus;
    }

    public static List<Status> getStatus(){
        return List.of(ACTIVE, INACTIVE, BLOCKED, EXPIRED);
    }
}
