package com.abcm0018.sai.shift.domain.enums;

import lombok.Getter;

@Getter
public enum ShiftType {
	MORNING("Mañana"),
	AFTERNOON("Tarde"),
	NIGHT("Noche");
	private final String displayName;

	ShiftType(String displayName) {
		this.displayName = displayName;
	}
}
