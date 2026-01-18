package com.abcm0018.sai.workshift.domain.specifications;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.abcm0018.sai.shift.domain.enums.ShiftType;
import com.abcm0018.sai.workshift.domain.entity.Workshift;

import jakarta.persistence.criteria.Predicate;

/**
 * Encapsula la lógica de negocio de cada filtro
 */
public class WorkshiftSpecifications {
	// Filtro por Usuario (Join implícito o acceso directo al ID)
	public static Specification<Workshift> hasUserId(Long userId) {
		return (root, query, cb) -> {
			if (userId == null) return null;
			return cb.equal(root.get("user").get("id"), userId);
		};
	}

	public static Specification<Workshift> hasShiftId(Long shiftId) {
		return (root, query, cb) -> {
			if (shiftId == null) return null;
			return cb.equal(root.get("shift").get("id"), shiftId);
		};
	}

	public static Specification<Workshift> hasShiftType(String shiftType) {
		return (root, query, cb) -> {
			if (shiftType == null) return null;
			return cb.equal(root.get("shift").get("shiftType"), ShiftType.valueOf(shiftType));
		};
	}

	// Filtros de Fecha
	public static Specification<Workshift> hasExactDate(LocalDate date) {
		return (root, query, cb) -> cb.equal(root.get("date"), date);
	}

	// Filtro: Rango de fechas
	public static Specification<Workshift> hasDateBetween(LocalDate startDate, LocalDate endDate) {
		return (root, query, cb) -> {
			if (startDate == null || endDate == null) return null;
			return cb.between(root.get("date"), startDate, endDate);
		};
	}

	// Filtro Lógico (Hoy/Pasado/Futuro)
	public static Specification<Workshift> isPeriod(Boolean isToday, Boolean isPast, Boolean isFuture) {
		return (root, query, cb) -> {
			LocalDate now = LocalDate.now();
			List<Predicate> predicates = new ArrayList<>();

			if (Boolean.TRUE.equals(isToday)) predicates.add(cb.equal(root.get("date"), now));
			if (Boolean.TRUE.equals(isPast)) predicates.add(cb.lessThan(root.get("date"), now));
			if (Boolean.TRUE.equals(isFuture)) predicates.add(cb.greaterThan(root.get("date"), now));

			if (predicates.isEmpty()) return null;
			return cb.or(predicates.toArray(new Predicate[0]));
		};
	}
}
