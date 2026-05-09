package com.abcm0018.sai.palets.application.service.helpers;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Utilidad estática para cálculos y análisis de estadísticas de producción
 * <p>
 * Responsabilidades:
 * - Calcular promedios y tendencias
 * - Análisis de producción
 * - Comparativas y benchmarks
 * - Construcción de reportes
 * <p>
 * NOTA: Clase de utilidad pura sin estado (métodos estáticos)
 */
public final class StatsHelper {

	private StatsHelper() {
		throw new AssertionError("No se puede instanciar StatsHelper");
	}

	/**
	 * Calcula el promedio de una lista de valores
	 */
	public static Double calculateAverage(List<Long> values) {
		if (values == null || values.isEmpty()) {
			return 0.0;
		}

		long sum = values.stream().mapToLong(Long::longValue).sum();
		double average = (double) sum / values.size();
		return Math.round(average * 100.0) / 100.0;
	}

	/**
	 * Calcula el promedio redondeado a 2 decimales
	 */
	public static Double calculateAverageRounded(double value) {
		return Math.round(value * 100.0) / 100.0;
	}

	/**
	 * Calcula la desviación estándar de una lista de valores
	 */
	public static Double calculateStandardDeviation(List<Long> values) {
		if (values == null || values.size() < 2) {
			return 0.0;
		}

		double average = calculateAverage(values);
		double sumOfSquares = values.stream()
				.mapToDouble(v -> Math.pow(v - average, 2))
				.sum();

		double variance = sumOfSquares / values.size();
		return Math.round(Math.sqrt(variance) * 100.0) / 100.0;
	}

	/**
	 * Calcula el crecimiento porcentual entre dos valores
	 */
	public static Double calculatePercentageGrowth(Long oldValue, Long newValue) {
		if (oldValue == null || oldValue == 0) {
			return 0.0;
		}

		double growth = ((double) (newValue != null ? newValue : 0) - oldValue) / oldValue * 100;
		return Math.round(growth * 100.0) / 100.0;
	}

	/**
	 * Calcula el porcentaje de un valor respecto a un total
	 */
	public static Double calculatePercentage(Long value, Long total) {
		if (total == null || total == 0) {
			return 0.0;
		}

		double percentage = ((double) (value != null ? value : 0) / total) * 100;
		return Math.round(percentage * 100.0) / 100.0;
	}

	/**
	 * Encuentra el valor máximo de una lista
	 */
	public static Optional<Long> findMax(List<Long> values) {
		if (values == null || values.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(values.stream().mapToLong(Long::longValue).max().orElse(0));
	}

	/**
	 * Encuentra el valor mínimo de una lista
	 */
	public static Optional<Long> findMin(List<Long> values) {
		if (values == null || values.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(values.stream().mapToLong(Long::longValue).min().orElse(0));
	}

	/**
	 * Construye un resumen estadístico de una lista de valores
	 */
	public static Map<String, Object> buildStatsSummary(String label, List<Long> values) {
		Map<String, Object> summary = new HashMap<>();

		if (values == null || values.isEmpty()) {
			summary.put("label", label);
			summary.put("count", 0);
			summary.put("sum", 0);
			summary.put("average", 0.0);
			summary.put("max", null);
			summary.put("min", null);
			summary.put("stdDev", 0.0);
			return summary;
		}

		long sum = values.stream().mapToLong(Long::longValue).sum();
		Double average = calculateAverage(values);
		Optional<Long> max = findMax(values);
		Optional<Long> min = findMin(values);
		Double stdDev = calculateStandardDeviation(values);

		summary.put("label", label);
		summary.put("count", values.size());
		summary.put("sum", sum);
		summary.put("average", average);
		summary.put("max", max.orElse(null));
		summary.put("min", min.orElse(null));
		summary.put("stdDev", stdDev);

		return summary;
	}

	/**
	 * Calcula el rango de fechas en días
	 */
	public static Long calculateDateRangeInDays(LocalDate startDate, LocalDate endDate) {
		if (startDate == null || endDate == null) {
			return 0L;
		}

		if (startDate.isAfter(endDate)) {
			return 0L;
		}

		return ChronoUnit.DAYS.between(startDate, endDate);
	}

	/**
	 * Calcula el número de días hábiles (excluyendo fines de semana)
	 */
	public static Long calculateBusinessDays(LocalDate startDate, LocalDate endDate) {
		if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
			return 0L;
		}

		long businessDays = 0;
		LocalDate currentDate = startDate;

		while (!currentDate.isAfter(endDate)) {
			int dayOfWeek = currentDate.getDayOfWeek().getValue();
			if (dayOfWeek < 6) { // Lunes a viernes = 1 a 5
				businessDays++;
			}
			currentDate = currentDate.plusDays(1);
		}

		return businessDays;
	}

	/**
	 * Construye un índice de productividad (palets por día hábil)
	 */
	public static Map<String, Object> buildProductivityIndex(Long totalPalets, LocalDate startDate, LocalDate endDate) {
		Map<String, Object> index = new HashMap<>();

		Long businessDays = calculateBusinessDays(startDate, endDate);
		Double productivityPerDay = businessDays > 0
				? calculateAverageRounded((double) (totalPalets != null ? totalPalets : 0) / businessDays)
				: 0.0;

		index.put("totalPalets", totalPalets != null ? totalPalets : 0);
		index.put("startDate", startDate);
		index.put("endDate", endDate);
		index.put("businessDays", businessDays);
		index.put("productivityPerDay", productivityPerDay);
		index.put("trend", determineTrend(productivityPerDay));

		return index;
	}

	/**
	 * Determina la tendencia de un valor (UP, DOWN, STABLE)
	 */
	public static String determineTrend(Double currentValue) {
		if (currentValue == null) {
			return "UNKNOWN";
		}

		if (currentValue > 50) {
			return "UP";
		} else if (currentValue < 20) {
			return "DOWN";
		} else {
			return "STABLE";
		}
	}

	/**
	 * Calcula el ranking de un valor en una lista (posición)
	 */
	public static Integer calculateRanking(Long value, List<Long> values) {
		if (value == null || values == null || values.isEmpty()) {
			return null;
		}

		List<Long> sorted = values.stream()
				.sorted((a, b) -> Long.compare(b, a)) // Descendente
				.toList();

		for (int i = 0; i < sorted.size(); i++) {
			if (sorted.get(i).equals(value)) {
				return i + 1;
			}
		}

		return null;
	}

	/**
	 * Calcula el percentil de un valor en una lista
	 */
	public static Double calculatePercentile(Long value, List<Long> values) {
		if (value == null || values == null || values.isEmpty()) {
			return 0.0;
		}

		long countBelow = values.stream().filter(v -> v < value).count();
		return calculateAverageRounded((double) countBelow / values.size() * 100);
	}
}