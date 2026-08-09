package com.abcm0018.sai.scanstation.application.dtos;

import com.abcm0018.sai.shift.domain.enums.ShiftType;
import com.fasterxml.jackson.annotation.JsonProperty;
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
	@JsonProperty("station_code")
	@Size(max = 50, message = "stationCode must not exceed 50 characters")
	private String stationCode;

	@JsonProperty("location_desc")
	@Size(max = 30, message = "locationDesc must not exceed 30 characters")
	private String locationDesc;

	@JsonProperty("camera_1_id")
	@Size(max = 20, message = "camera1Id must not exceed 20 characters")
	private String camera1Id;

	@JsonProperty("camera_2_id")
	@Size(max = 20, message = "camera2Id must not exceed 20 characters")
	private String camera2Id;
}
