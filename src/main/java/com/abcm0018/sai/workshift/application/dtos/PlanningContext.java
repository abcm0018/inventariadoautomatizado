package com.abcm0018.sai.workshift.application.dtos;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.abcm0018.sai.shift.domain.entity.Shift;
import com.abcm0018.sai.shift.domain.enums.ShiftType;
import com.abcm0018.sai.users.domain.entity.User;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PlanningContext {
	// Datos maestros
	private List<LocalDate> workingDays;
	private List<User> operators;
	private List<Shift> activeShifts;

	private Map<Long, Set<LocalDate>> occupiedDatesMap;
	private Map<Long, ShiftType> latestShiftTypeMap;

	/**
	 * Determines whether a collision exists for a given user on a specified date.
	 * A collision occurs if the user has already occupied the given date in the
	 * scheduling context.
	 *
	 * @param userId the unique identifier of the user to check
	 * @param date the specific date to check for a scheduling conflict
	 * @return true if a collision exists for the user on the given date, false otherwise
	 */
	public boolean hasCollision(Long userId, LocalDate date) {
		if (occupiedDatesMap == null || occupiedDatesMap.isEmpty()) return false;

		return occupiedDatesMap.getOrDefault(userId, Set.of()).contains(date);
	}

	/**
	 * Retrieves the most recent shift type assigned to a specific user.
	 * If no information is available or the user has no previously assigned shift type, null is returned.
	 *
	 * @param userId the unique identifier of the user whose latest shift type is being queried
	 * @return the latest {@link ShiftType} assigned to the specified user, or null if no assignment exists
	 */
	public ShiftType getLastShiftType(Long userId) {
		if (latestShiftTypeMap == null || latestShiftTypeMap.isEmpty()) return null;

		return latestShiftTypeMap.getOrDefault(userId, null);
	}
}
