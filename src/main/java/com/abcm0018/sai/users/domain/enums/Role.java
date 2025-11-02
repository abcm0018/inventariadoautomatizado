package com.abcm0018.sai.users.domain.enums;

import lombok.Getter;

@Getter
public enum Role {
    ADMIN("Administrador"), //Administrador
    OPERATOR("Operador"), //Operador
    SUPERVISOR("Supervisor"); //Supervisor

	private final String displayName;

	Role(String displayName) {
		this.displayName = displayName;
	}
}
