package com.abcm0018.sai.shift.domain.enums;

import lombok.Getter;

@Getter
public enum ShiftType {
	MORNING("Mañana"),
	AFTERNOON("Tarde"),
	NIGHT("Noche"),
	SPLIT("Partido"); // Por si hay turnos partidos

	private final String displayName;

	ShiftType(String displayName) {
		this.displayName = displayName;
	}
}
