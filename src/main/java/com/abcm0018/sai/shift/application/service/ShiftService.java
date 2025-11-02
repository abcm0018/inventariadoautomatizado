package com.abcm0018.sai.shift.application.service;

import java.time.LocalTime;
import java.util.List;

import com.abcm0018.sai.shift.application.dtos.ShiftRequestDTO;
import com.abcm0018.sai.shift.application.dtos.ShiftResponseDTO;
import com.abcm0018.sai.shift.application.dtos.ShiftStatusChangeDTO;
import com.abcm0018.sai.shift.application.dtos.ShiftSummaryDTO;
import com.abcm0018.sai.shift.domain.enums.ShiftType;

public interface ShiftService {

	ShiftResponseDTO createShift(ShiftRequestDTO request);
	ShiftResponseDTO findById(Long id);
	ShiftResponseDTO findByShiftType(ShiftType shiftType);
	List<ShiftSummaryDTO> findAll();
	List<ShiftSummaryDTO> findAllOrderedByStartTime();
	ShiftResponseDTO updateShift(Long id, ShiftRequestDTO requestDTO);
	void deleteShift(Long id);
	void permanentlyDeleteShift(Long id);

	ShiftResponseDTO activateShift(Long id);
	ShiftResponseDTO deactivateShift(Long id, ShiftStatusChangeDTO statusChangeDTO);

	List<ShiftSummaryDTO> findAllActiveShifts();
	List<ShiftSummaryDTO> findActiveShiftsOrdered();
	List<ShiftSummaryDTO> findInactiveShifts();
	Long countActiveShifts();

	ShiftResponseDTO findShiftAtTime(LocalTime time);
	List<ShiftSummaryDTO> findShiftsCrossingMidnight();
	ShiftResponseDTO getCurrentShift();

	List<ShiftSummaryDTO> findShiftsByDuration(long hours);
	List<ShiftSummaryDTO> findShiftsWithMinDuration(long minHours);

	boolean existsByShiftType(ShiftType shiftType);

	void createDefaultShifts();
}
