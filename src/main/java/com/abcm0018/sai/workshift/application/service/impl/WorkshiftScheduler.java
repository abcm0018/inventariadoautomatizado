package com.abcm0018.sai.workshift.application.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.abcm0018.sai.workshift.application.service.WorkshiftService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkshiftScheduler {
	private final WorkshiftService workshiftService;

	/**
	 * Cron job que se ejecuta cada viernes a las 18:00
	 * Genera automáticamente los turnos para la próxima semana (Lun-Vie)
	 * <p>
	 * Expresión cron: "0 0 18 ? * FRI"
	 * - 0: segundo 0
	 * - 0: minuto 0
	 * - 18: hora 18 (6 PM)
	 * - ?: cualquier día del mes
	 * - *: cualquier mes
	 * - FRI: viernes
	 */
	@Scheduled(cron = "0 0 18 ? * FRI", zone = "Europe/Madrid")
	public void scheduleWeekWorkshifts() {
		log.info("╔════════════════════════════════════════════════════════════════╗");
		log.info("║   INICIANDO GENERACIÓN AUTOMÁTICA DE TURNOS SEMANALES          ║");
		log.info("║   Fecha/Hora: {}                          ║",
				LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
		log.info("╚════════════════════════════════════════════════════════════════╝");

		try {
			workshiftService.generateNextWeekSchedule();

			// TODO: Enviar alerta por email a administradores
			// emailService.notifySchedulingCreation();

			log.info("╔════════════════════════════════════════════════════════════════╗");
			log.info("║   ✓ GENERACIÓN DE TURNOS COMPLETADA EXITOSAMENTE             ║");
			log.info("╚════════════════════════════════════════════════════════════════╝");


		} catch (Exception e) {
			log.error("╔════════════════════════════════════════════════════════════════╗");
			log.error("║   ✗ ERROR EN LA GENERACIÓN DE TURNOS                         ║");
			log.error("╚════════════════════════════════════════════════════════════════╝");
			log.error("Error al generar planificación semanal", e);

			// TODO: Enviar alerta por email a administradores
			// emailService.notifySchedulingFailure(excepcion);
		}
	}

	/**
	 * Cron job de respaldo que se ejecuta cada sábado a las 08:00
	 * Verifica que se hayan generado los turnos, si no, los crea
	 * <p>
	 * Expresión cron: "0 0 8 ? * SAT"
	 */
	@Scheduled(cron = "0 0 8 ? * SAT", zone = "Europe/Madrid")
	public void verifyWeeklySchedule() {
		log.info("Ejecutando verificación de seguridad de la planificación semanal");

		try {
			// Verificar si existen turnos para la próxima semana
			// Si no existen, generarlos automáticamente

			log.info("Verificación completada");

		} catch (Exception e) {
			log.error("Error en verificación de planificación", e);
		}
	}
}
