package com.abcm0018.sai.email.domain.events;

import lombok.Getter;
import lombok.Setter;

import org.springframework.context.ApplicationEvent;

import java.time.LocalDate;

/**
 * Evento publicado cuando se cancela un turno
 */
@Getter
@Setter
public class WorkshiftCanceledEvent extends ApplicationEvent {
	private String employeeName;
	private LocalDate shiftDate;
	private String shiftType;
	private String cancellationReason;
	private String employeeEmail;

	public WorkshiftCanceledEvent(Object source, String employeeName, LocalDate shiftDate,
			String shiftType, String cancellationReason, String employeeEmail) {
		super(source);
		this.employeeName = employeeName;
		this.shiftDate = shiftDate;
		this.shiftType = shiftType;
		this.cancellationReason = cancellationReason;
		this.employeeEmail = employeeEmail;
	}

	public WorkshiftCanceledEvent() {
		super("default-source");
	}
}
