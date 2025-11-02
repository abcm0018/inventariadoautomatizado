package com.abcm0018.sai.email.domain.events;

import lombok.Getter;
import lombok.Setter;

import org.springframework.context.ApplicationEvent;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Evento publicado cuando se asigna un turno a un empleado
 */
@Getter
@Setter
public class WorkshiftAssignedEvent extends ApplicationEvent {
	private String employeeName;
	private LocalDate shiftDate;
	private String shiftType;
	private LocalTime startTime;
	private LocalTime endTime;
	private String employeeEmail;

	public WorkshiftAssignedEvent(Object source, String employeeName, LocalDate shiftDate,
			String shiftType, LocalTime startTime, LocalTime endTime,
			String employeeEmail) {
		super(source);
		this.employeeName = employeeName;
		this.shiftDate = shiftDate;
		this.shiftType = shiftType;
		this.startTime = startTime;
		this.endTime = endTime;
		this.employeeEmail = employeeEmail;
	}

	public WorkshiftAssignedEvent() {
		super("default-source");
	}
}
