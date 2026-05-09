package com.abcm0018.sai.scanstation.application.dtos;

import com.abcm0018.sai.shift.domain.enums.ShiftType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScanStationRequestDTO {
	@Size(max = 50, message = "stationCode must not exceed 50 characters")
	private String stationCode;

	@Size(max = 20, message = "locationDesc must not exceed 20 characters")
	private String locationDesc;

	@Size(max = 20, message = "camera1Id must not exceed 20 characters")
	private String camera1Id;

	@Size(max = 20, message = "camera2Id must not exceed 20 characters")
	private String camera2Id;
}
