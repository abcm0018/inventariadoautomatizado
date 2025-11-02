package com.abcm0018.sai.palets.application.service.helpers;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilidad estática para cálculos y validaciones de caducidad
 * <p>
 * Responsabilidades:
 * - Calcular fechas del umbral de caducidad
 * - Determinar estado de expiración
 * - Generar recomendaciones por caducidad
 * - Analizar palets vencidos
 * <p>
 * NOTA: Clase de utilidad pura sin estado (métodos estáticos)
 */
public final class ExpiryHelper {

	private static final int EXPIRY_WARNING_DAYS = 7;
	private static final int CRITICAL_EXPIRY_DAYS = 3;

	// Constructor privado para prevenir instanciación
	private ExpiryHelper() {
		throw new AssertionError("No se puede instanciar ExpiryHelper");
	}

	/**
	 * Obtiene la fecha umbral para palets próximos a caducar (7 días)
	 */
	public static LocalDate getWarningThreshold() {
		return LocalDate.now().plusDays(EXPIRY_WARNING_DAYS);
	}

	/**
	 * Obtiene la fecha umbral para caducidad crítica (3 días)
	 */
	public static LocalDate getCriticalThreshold() {
		return LocalDate.now().plusDays(CRITICAL_EXPIRY_DAYS);
	}

	/**
	 * Calcula los días hasta la expiración
	 *
	 * @param expiryDate fecha de vencimiento del palet
	 * @return número de días hasta vencer (negativo si ya expiró)
	 */
	public static Long calculateDaysUntilExpiry(LocalDate expiryDate) {
		if (expiryDate == null) {
			return null;
		}
		return ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
	}

	/**
	 * Determina el estado de caducidad textual
	 *
	 * @param expiryDate fecha de vencimiento
	 * @return uno de: EXPIRED, CRITICAL, WARNING, FRESH, UNKNOWN
	 */
	public static String getExpiryStatus(LocalDate expiryDate) {
		if (expiryDate == null) {
			return "UNKNOWN";
		}

		Long days = calculateDaysUntilExpiry(expiryDate);

		if (days == null) {
			return "UNKNOWN";
		}

		if (days < 0) {
			return "EXPIRED";
		} else if (days <= CRITICAL_EXPIRY_DAYS) {
			return "CRITICAL";
		} else if (days <= EXPIRY_WARNING_DAYS) {
			return "WARNING";
		}
		return "FRESH";
	}

	/**
	 * Verifica si un palet está expirado
	 */
	public static boolean isExpired(LocalDate expiryDate) {
		if (expiryDate == null) {
			return false;
		}
		return expiryDate.isBefore(LocalDate.now());
	}

	/**
	 * Verifica si un palet está en estado crítico (próximo a expirar en 3 días)
	 */
	public static boolean isCritical(LocalDate expiryDate) {
		if (expiryDate == null) {
			return false;
		}
		Long days = calculateDaysUntilExpiry(expiryDate);
		return days != null && days <= CRITICAL_EXPIRY_DAYS && days > 0;
	}

	/**
	 * Verifica si un palet está próximo a caducar (7 días)
	 */
	public static boolean isExpiringSoon(LocalDate expiryDate) {
		if (expiryDate == null) {
			return false;
		}
		Long days = calculateDaysUntilExpiry(expiryDate);
		return days != null && days <= EXPIRY_WARNING_DAYS && days > 0;
	}

	/**
	 * Desglose de palets por estado de caducidad
	 */
	public static ExpiryBreakdown analyzeExpiryStatus(List<LocalDate> expiryDates) {
		long expired = 0;
		long critical = 0;
		long warning = 0;
		long fresh = 0;

		for (LocalDate date : expiryDates) {
			if (isExpired(date)) {
				expired++;
			} else if (isCritical(date)) {
				critical++;
			} else if (isExpiringSoon(date)) {
				warning++;
			} else {
				fresh++;
			}
		}

		return new ExpiryBreakdown(expired, critical, warning, fresh);
	}

	/**
	 * Record para contener desglose de caducidad
	 */
	public record ExpiryBreakdown(long expired, long critical, long warning, long fresh) {
		public long total() {
			return expired + critical + warning + fresh;
		}
	}

	/**
	 * Genera recomendaciones basadas en análisis de caducidad
	 */
	public static List<String> generateExpiryRecommendations(long totalStock, long expiredCount,
			long criticalCount, long warningCount) {
		List<String> recommendations = new ArrayList<>();

		if (expiredCount > 0) {
			recommendations.add(expiredCount + " palet(s) EXPIRADO(S) - Retirar del inventario de inmediato");
		}

		if (criticalCount > 0) {
			recommendations.add(criticalCount + " palet(s) en estado CRÍTICO (≤3 días) - Priorizar en despachos urgentes");
		}

		if (warningCount > 0) {
			recommendations.add(warningCount + " palet(s) próximo(s) a caducar (≤7 días) - Planificar despachos");
		}

		if (totalStock > 0 && expiredCount == 0 && criticalCount == 0 && warningCount == 0) {
			recommendations.add("Inventario de caducidad en estado óptimo");
		}

		return recommendations;
	}
}