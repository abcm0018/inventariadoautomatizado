package com.abcm0018.sai.palets.application.service.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utilidad estática para análisis y gestión de inventario
 * <p>
 * Responsabilidades:
 * - Calcular estado de salud del inventario
 * - Generar detalles de análisis de inventario
 * - Análisis de porcentajes y umbrales
 * - Recomendaciones de inventario
 * <p>
 * NOTA: Clase de utilidad pura sin estado (métodos estáticos)
 */
public final class InventoryHelper {

	private InventoryHelper() {
		throw new AssertionError("No se puede instanciar InventoryHelper");
	}

	/**
	 * Determina el estado de salud del inventario
	 * <p>
	 * Estados posibles: CRITICAL, OUT_OF_STOCK, WARNING, HEALTHY
	 * <p>
	 * Reglas:
	 * - CRITICAL: Hay palets expirados
	 * - OUT_OF_STOCK: Sin stock disponible
	 * - WARNING: Más del 30% próximo a caducar
	 * - HEALTHY: Inventario en estado óptimo
	 */
	public static String determineHealthStatus(Long totalStock, Long expiringSoon, Long expired) {
		if (totalStock == null || totalStock < 0) {
			return "UNKNOWN";
		}

		if (expired != null && expired > 0) {
			return "CRITICAL";
		}

		if (totalStock == 0) {
			return "OUT_OF_STOCK";
		}

		// Más del 30% próximo a caducar
		if (expiringSoon != null && expiringSoon > totalStock * 0.3) {
			return "WARNING";
		}

		return "HEALTHY";
	}

	/**
	 * Calcula el porcentaje de palets en estado crítico
	 */
	public static Double calculateCriticalPercentage(long criticalCount, long totalStock) {
		if (totalStock == 0) {
			return 0.0;
		}
		return Math.round((double) criticalCount / totalStock * 10000.0) / 100.0;
	}

	/**
	 * Calcula el porcentaje de palets próximos a caducar
	 */
	public static Double calculateExpiringPercentage(long expiringCount, long totalStock) {
		return calculateCriticalPercentage(expiringCount, totalStock);
	}

	/**
	 * Evalúa si el nivel de inventario es bajo
	 * Umbral: menos del 20% de la capacidad promedio esperada
	 */
	public static boolean isLowStock(Long currentStock, Long averageCapacity) {
		if (averageCapacity == null || averageCapacity == 0) {
			return currentStock != null && currentStock < 50;
		}
		return currentStock != null && currentStock < (averageCapacity * 0.2);
	}

	/**
	 * Evalúa si el nivel de inventario es alto
	 * Umbral: más del 80% de la capacidad promedio esperada
	 */
	public static boolean isHighStock(Long currentStock, Long averageCapacity) {
		if (averageCapacity == null || averageCapacity == 0) {
			return currentStock != null && currentStock > 100;
		}
		return currentStock != null && currentStock > (averageCapacity * 0.8);
	}

	/**
	 * Construye un desglose por estado de inventario
	 */
	public static Map<String, Object> buildHealthReport(Long totalStock, Long expiringSoon,
			Long expired, Long critical) {
		Map<String, Object> report = new HashMap<>();

		report.put("totalStock", totalStock != null ? totalStock : 0);
		report.put("expiringPalets", expiringSoon != null ? expiringSoon : 0);
		report.put("expiredPalets", expired != null ? expired : 0);
		report.put("criticalPalets", critical != null ? critical : 0);

		long healthyStock = (totalStock != null ? totalStock : 0)
				- (expiringSoon != null ? expiringSoon : 0)
				- (expired != null ? expired : 0)
				- (critical != null ? critical : 0);
		report.put("healthyPalets", Math.max(0, healthyStock));

		// Porcentajes
		if (totalStock != null && totalStock > 0) {
			report.put("expiringPercentage", calculateExpiringPercentage(expiringSoon != null ? expiringSoon : 0, totalStock));
			report.put("expiredPercentage", calculateExpiringPercentage(expired != null ? expired : 0, totalStock));
			report.put("criticalPercentage", calculateCriticalPercentage(critical != null ? critical : 0, totalStock));
		} else {
			report.put("expiringPercentage", 0.0);
			report.put("expiredPercentage", 0.0);
			report.put("criticalPercentage", 0.0);
		}

		report.put("healthStatus", determineHealthStatus(totalStock, expiringSoon, expired));

		return report;
	}

	/**
	 * Genera recomendaciones basadas en el estado del inventario
	 */
	public static List<String> generateInventoryRecommendations(Long totalStock, long expiredCount,
			long criticalCount, long warningCount) {
		List<String> recommendations = new ArrayList<>();

		if (totalStock == null || totalStock == 0) {
			recommendations.add("SIN STOCK - Programar producción urgente");
		} else if (totalStock < 20) {
			recommendations.add("NIVEL BAJO de inventario (" + totalStock + " palets) - Aumentar producción");
		}

		if (expiredCount > 0) {
			recommendations.add("RIESGO CRÍTICO: " + expiredCount + " palet(s) expirado(s) - Retirar inmediatamente");
		}

		if (criticalCount > 0) {
			recommendations.add("URGENTE: " + criticalCount + " palet(s) en estado crítico - Despachos prioritarios");
		}

		if (warningCount > 0) {
			recommendations.add("ALERTA: " + warningCount + " palet(s) próximo a caducar - Planificar despachos");
		}

		if (totalStock != null && totalStock > 100) {
			recommendations.add("SOBRESTOCK: " + totalStock + " palets - Considerar aumento de despachos");
		}

		if (totalStock != null && totalStock > 0 && expiredCount == 0 && criticalCount == 0 && warningCount == 0) {
			recommendations.add("Inventario en estado óptimo - Continuar monitoreo regular");
		}

		return recommendations;
	}

	/**
	 * Calcula días promedio en inventario
	 */
	public static Double calculateAverageDaysInStock(List<Long> daysInStockList) {
		if (daysInStockList == null || daysInStockList.isEmpty()) {
			return 0.0;
		}

		long sum = daysInStockList.stream().mapToLong(Long::longValue).sum();
		return Math.round((double) sum / daysInStockList.size() * 100.0) / 100.0;
	}

	/**
	 * Evalúa rotación de inventario (palets salientes vs. stock)
	 */
	public static Double calculateInventoryRotation(Long paletsMoved, Long currentStock) {
		if (currentStock == null || currentStock == 0) {
			return 0.0;
		}
		return Math.round((double) (paletsMoved != null ? paletsMoved : 0) / currentStock * 100.0) / 100.0;
	}
}