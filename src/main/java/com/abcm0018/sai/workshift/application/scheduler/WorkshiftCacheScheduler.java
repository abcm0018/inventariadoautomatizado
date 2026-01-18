package com.abcm0018.sai.workshift.application.scheduler;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.abcm0018.sai.workshift.domain.entity.Workshift;
import com.abcm0018.sai.workshift.domain.repository.WorkshiftRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Scheduler para pre-cargar los workshifts del día en Redis
 * Se ejecuta diariamente a las 00:00 para optimizar el rendimiento
 * durante las operaciones de escaneo de palets
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WorkshiftCacheScheduler {
	private final WorkshiftRepository workshiftRepository;
	private final RedisTemplate<String, Long> workshiftRedisTemplate;

	// Constantes
	private static final String WORKSHIFT_CACHE_KEY_PATTERN = "workshift:user:%d:date:%s";
	private static final Duration CACHE_TTL = Duration.ofHours(24);

	/**
	 * Precarga todos los workshifts del día actual en Redis
	 * Cron: Todos los días a las 00:00
	 * Timezone: Europe/Madrid
	 */
	@Scheduled(cron = "0 0 0 * * ?", zone = "Europe/Madrid")
	@Transactional
	public void preloadTodayWorkshifts() {
		log.info("╔══════════════════════════════════════════════════════════════╗");
		log.info("║   INICIANDO PRE-CARGA DE WORKSHIFTS DEL DÍA EN REDIS         ║");
		log.info("║   Fecha/Hora: {}                                             ║", LocalDateTime.now());
		log.info("╚══════════════════════════════════════════════════════════════╝");

		try {
			LocalDate today = LocalDate.now();

			// Obtener todos los workshifts del día

			List<Workshift> todayWorkshifts = workshiftRepository.findByDate(today);

			if (todayWorkshifts.isEmpty()) {
				log.warn("No se encontraron workshifts para la fecha: {}", today);
				log.warn("Asegúrate de que el generador semanal se ejecutó correctamente");
				return;
			}

			int cached = 0;
			int failed = 0;

			for (Workshift workshift : todayWorkshifts) {
				try {
					String cacheKey = buildCacheKey(workshift.getUser().getId(), today);

					// Cachear workshift_id en Redis con TTL de 24 horas
					workshiftRedisTemplate.opsForValue().set(
							cacheKey,
							workshift.getId(),
							CACHE_TTL
					);

					cached++;

					log.debug("✓ Workshift cacheado - Usuario: {} ({}), Turno: {}, Key: {}",
							workshift.getUser().getFullName(),
							workshift.getUser().getId(),
							workshift.getShift().getShiftType().getDisplayName(),
							cacheKey
					);

				} catch (Exception e) {
					failed++;
					log.error("✗ Error cacheando workshift {} para usuario {}",
							workshift.getId(),
							workshift.getUser().getId(),
							e
					);
				}
			}

			log.info("╔══════════════════════════════════════════════════════════════╗");
			log.info("║   ✓ PRE-CARGA COMPLETADA EXITOSAMENTE                        ║");
			log.info("║   Total workshifts: {}                                       ║", todayWorkshifts.size());
			log.info("║   Cacheados: {}                                              ║", cached);
			log.info("║   Fallidos: {}                                               ║", failed);
			log.info("║   Fecha: {}                                                  ║", today);
			log.info("║   TTL: 24 horas                                              ║");
			log.info("╚══════════════════════════════════════════════════════════════╝");

		} catch (Exception e) {
			log.error("╔══════════════════════════════════════════════════════════════╗");
			log.error("║   ✗ ERROR EN PRE-CARGA DE WORKSHIFTS                         ║");
			log.error("╚══════════════════════════════════════════════════════════════╝");
			log.error("Error durante la pre-carga de workshifts", e);
		}
	}

	/**
	 * Limpia la caché de workshifts del día anterior
	 * Cron: Todos los días a las 00:30 (30 minutos después de la precarga)
	 */
	@Scheduled(cron = "0 30 0 * * ?", zone = "Europe/Madrid")
	public void cleanYesterdayCache() {
		log.info("Limpiando caché de workshifts del día anterior...");

		try {
			LocalDate yesterday = LocalDate.now().minusDays(1);

			// Obtener todos los workshifts del día anterior
			List<Workshift> yesterdayWorkshifts = workshiftRepository.findByDate(yesterday);

			int deleted = 0;

			for (Workshift workshift : yesterdayWorkshifts) {
				String cacheKey = buildCacheKey(workshift.getUser().getId(), yesterday);

				Boolean wasDeleted = workshiftRedisTemplate.delete(cacheKey);
				if (wasDeleted) {
					deleted++;
				}
			}

			log.info("Limpieza completada - {} claves eliminadas", deleted);

		} catch (Exception e) {
			log.error("Error durante la limpieza de caché", e);
		}
	}

	/**
	 * Fuerza la precarga (útil para testing)
	 * Puede ser llamado desde un endpoint de admin
	 */
	@Transactional
	public void forcePreload() {
		log.info("Pre-carga manual forzada (HOY + PRÓXIMA SEMANA)");

		// 1. Carga los de hoy
		doPreloadForDateRange(LocalDate.now(), LocalDate.now(), CACHE_TTL);

		// 2. Carga los de la próxima semana
		LocalDate nextMonday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
		LocalDate nextFriday = nextMonday.plusDays(4);

		Duration cacheTTL = Duration.ofDays(7);
		doPreloadForDateRange(nextMonday, nextFriday, cacheTTL);
	}

	/**
	 * Obtiene estadísticas de la caché
	 */
	public CacheStatistics getCacheStatistics() {
		LocalDate today = LocalDate.now();
		LocalDate nextMonday = today.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
		LocalDate nextFriday = nextMonday.plusDays(4);

		List<Workshift> todayWorkshifts = workshiftRepository.findByDate(today);
		List<Workshift> nextWeekWorkshifts = workshiftRepository.findByDateBetweenWithUsers(nextMonday, nextFriday);

		int totalWorkshifts = todayWorkshifts.size();
		int cached = 0;

		for (Workshift workshift : nextWeekWorkshifts) {
			String cacheKey = buildCacheKey(workshift.getUser().getId(), workshift.getDate());
			if (workshiftRedisTemplate.hasKey(cacheKey)) {
				cached++;
			}
		}

		return CacheStatistics.builder()
				.totalWorkshifts(totalWorkshifts)
				.cachedWorkshifts(cached)
				.cacheHitRate(totalWorkshifts > 0 ? (double) cached / totalWorkshifts * 100 : 0.0)
				.date(today)
				.build();
	}

	/**
	 * Lógica de negocio REAL. No es transaccional por sí misma,
	 * sino que se une a la transacción del método PÚBLICO que la llame.
	 */
	private void doPreloadForDateRange(LocalDate startDate, LocalDate endDate, Duration ttl) {

		// 1. Obtener datos (Usando la consulta optimizada con JOIN FETCH)
		// Esta llamada se une a la transacción existente.
		List<Workshift> workshifts = workshiftRepository.findByDateBetweenWithUsers(startDate, endDate);


		if (workshifts.isEmpty()) {
			log.warn("No se encontraron turnos para el rango [{} - {}]. No se cacheará nada.", startDate, endDate);
			return;
		}

		log.info("Cacheando {} turnos para el rango [{} - {}]...", workshifts.size(), startDate, endDate);

		// 2. Iterar y cachear
		for (Workshift workshift : workshifts) {
			String cacheKey = buildCacheKey(workshift.getUser().getId(), workshift.getDate());
			workshiftRedisTemplate.opsForValue().set(cacheKey, workshift.getId(), ttl);
		}
	}

	/**
	 * Construye la clave de Redis para un usuario y fecha
	 */
	private String buildCacheKey(Long userId, LocalDate date) {
		return String.format(WORKSHIFT_CACHE_KEY_PATTERN, userId, date);
	}

	/**
	 * DTO para estadísticas de caché
	 */
	@lombok.Data
	@lombok.Builder
	public static class CacheStatistics {
		private int totalWorkshifts;
		private int cachedWorkshifts;
		private double cacheHitRate;
		private LocalDate date;
	}
}
