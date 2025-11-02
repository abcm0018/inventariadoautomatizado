package com.abcm0018.sai.shift.infrastructure.seeders;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.abcm0018.sai.shift.application.service.ShiftService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Poblador de datos (Data Seeder) para las plantillas de Turnos (Shifts)
 * <p>
 * Este componente se ejecuta al inciio de la aplicación y crea
 * los turnos básicos (Mañana, Tarde, Noche) si la base de datos
 * no contiene ningún turno.
 */
@Slf4j
@Component
@RequiredArgsConstructor
// No queremos que se ejecute durante las pruebas unitarias.
@Profile("!test")
public class ShiftDataSeeder implements CommandLineRunner {

	private final ShiftService shiftService;

	@Override
	public void run(String... args) throws Exception {
		log.info("Iniciando poblador de datos (Data Seeder) para las plantillas de Turnos (Shifts)");

		// Usamos la utilidad del servicio que cuente TODOS los turnos (activos e inactivos)
		// Si no los tienes, usamos findAll().isEmpty()
		if (!shiftService.findAll().isEmpty()) {
			log.info("✅ La base de datos de Shifts ya contiene datos. No se requiere poblamiento.");
			return;
		}

		log.info("⚠️ Base de datos de Shifts vacía. Creando turnos por defecto...");

		shiftService.createDefaultShifts();

		log.info("Turnos por defecto creados exitosamente");
	}
}
