package com.abcm0018.sai.workshift.infrastructure.test;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.abcm0018.sai.workshift.application.dtos.WeeklyScheduleResponseDTO;
import com.abcm0018.sai.workshift.application.service.WorkshiftService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@Profile("dev")
@RequiredArgsConstructor
@RequestMapping("/api/v1/test-scheduler")
public class TestSchedulerController {

	private final WorkshiftService workshiftService;

	@PostMapping("/generate-next-week")
	public ResponseEntity<WeeklyScheduleResponseDTO> triggerNextWeekGeneration() {
		log.info("--- SIMULADOR: Solicitud manual para generar turnos de la próxima semana ---");
		try {
			WeeklyScheduleResponseDTO response = workshiftService.generateNextWeekSchedule();
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			log.error("❌ SIMULADOR: Error en generación manual de turnos", e);
			return ResponseEntity.internalServerError().build();
		}
	}
}
