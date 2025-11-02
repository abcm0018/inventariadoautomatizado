package com.abcm0018.sai.email.domain.enums;

import lombok.Getter;

/**
 * Tipos de emails que el sistema puede enviar.
 * Cada tipo tiene su plantilla, asunto y parámetros específicos.
 */
@Getter
public enum EmailType {
	// AUTENTICACIÓN
	NEW_EMPLOYEE_CREDENTIALS("new_employee_credentials", "Bienvenida - Credenciales de acceso",
			"name", "surname", "employeeNumber", "password"),

	PASSWORD_RESET("password_reset", "Restablecimiento de contraseña",
			"name", "surname", "resetToken", "resetLink"),

	PASSWORD_CHANGED("password_changed", "Tu contraseña ha sido actualizada",
			"name", "surname", "changeDate"),

	ACCOUNT_LOCKED("account_locked", "Cuenta bloqueada por seguridad",
			"name", "surname", "unlockInstructions"),

	// ALERTAS DE INVENTARIO
	LOW_STOCK_ALERT("low_stock_alert", "Alerta: Stock bajo",
			"productName", "productEan", "currentStock", "minimumStock", "alertDate"),

	CRITICAL_STOCK_ALERT("critical_stock_alert", "ALERTA CRÍTICA: Stock crítico",
			"productName", "productEan", "currentStock", "supervisorName"),

	PALET_EXPIRY_SOON("palet_expiry_soon", "Aviso: Palets próximos a caducar",
			"productName", "batchNumber", "expiryDate", "daysRemaining", "reportDate"),

	PALET_EXPIRED("palet_expired", "Palets caducados - Acción requerida",
			"productName", "batchNumber", "ssccList", "expiryDate", "supervisorName"),

	// NOTIFICACIONES OPERATIVAS
	BATCH_RECEIVED("batch_received", "Lote recibido correctamente",
			"employeeName", "batchNumber", "quantity", "receivedDate", "operator"),

	WORKSHIFT_ASSIGNED("workshift_assigned", "Nuevo turno asignado",
			"employeeName", "shiftDate", "shiftType", "startTime", "endTime"),

	WORKSHIFT_CANCELED("workshift_canceled", "Turno cancelado",
			"employeeName", "shiftDate", "shiftType", "cancellationReason"),

	// REPORTES
	DAILY_REPORT("daily_report", "Reporte diario de inventario",
			"reportDate", "totalPalets", "productsCount", "criticalStockCount", "reportingManager");

	private final String templateName;
	private final String defaultSubject;
	private final String[] requiredParameters;

	EmailType(String templateName, String defaultSubject, String... requiredParameters) {
		this.templateName = templateName;
		this.defaultSubject = defaultSubject;
		this.requiredParameters = requiredParameters;
	}

}
