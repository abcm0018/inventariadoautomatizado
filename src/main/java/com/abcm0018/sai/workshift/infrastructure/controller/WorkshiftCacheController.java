package com.abcm0018.sai.workshift.infrastructure.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.abcm0018.sai.workshift.application.scheduler.WorkshiftCacheScheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controlador para gestionar la caché de workshifts
 * Solo accesible para administradores
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/workshift-cache")
@RequiredArgsConstructor
public class WorkshiftCacheController {
	private final WorkshiftCacheScheduler workshiftCacheScheduler;

	/**
	 * Fuerza una pre-carga manual de los workshifts del día
	 * POST /api/admin/workshift-cache/reload
	 */
	@PostMapping("/reload")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Map<String, Object>> forceReload() {
		log.info("🔄 Pre-carga manual solicitada por administrador");

		try {
			workshiftCacheScheduler.forcePreload();

			Map<String, Object> response = new HashMap<>();
			response.put("success", true);
			response.put("message", "Pre-carga de workshifts completada exitosamente");
			response.put("timestamp", LocalDate.now());

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			log.error("Error durante pre-carga manual", e);

			Map<String, Object> response = new HashMap<>();
			response.put("success", false);
			response.put("message", "Error durante la pre-carga: " + e.getMessage());

			return ResponseEntity.internalServerError().body(response);
		}
	}

	/**
	 * Obtiene estadísticas de la caché
	 * GET /api/admin/workshift-cache/statistics
	 */
	@GetMapping("/statistics")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<WorkshiftCacheScheduler.CacheStatistics> getCacheStatistics() {
		log.debug("Consultando estadísticas de caché de workshifts");

		WorkshiftCacheScheduler.CacheStatistics stats = workshiftCacheScheduler.getCacheStatistics();

		return ResponseEntity.ok(stats);
	}

	/**
	 * Verifica el estado de la caché
	 * GET /api/admin/workshift-cache/health
	 */
	@GetMapping("/health")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Map<String, Object>> checkHealth() {
		Map<String, Object> health = new HashMap<>();

		try {
			WorkshiftCacheScheduler.CacheStatistics stats = workshiftCacheScheduler.getCacheStatistics();

			health.put("status", "healthy");
			health.put("date", stats.getDate());
			health.put("totalWorkshifts", stats.getTotalWorkshifts());
			health.put("cachedWorkshifts", stats.getCachedWorkshifts());
			health.put("cacheHitRate", String.format("%.2f%%", stats.getCacheHitRate()));

			return ResponseEntity.ok(health);

		} catch (Exception e) {
			log.error("Error verificando salud de caché", e);

			health.put("status", "unhealthy");
			health.put("error", e.getMessage());

			return ResponseEntity.status(503).body(health);
		}
	}
}
