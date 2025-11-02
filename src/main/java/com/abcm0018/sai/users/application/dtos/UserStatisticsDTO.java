package com.abcm0018.sai.users.application.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Map;

/**
 * DTO con estadísticas globales de usuarios
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatisticsDTO {

	@JsonProperty("total_users")
	private Long totalUsers;

	@JsonProperty("active_users")
	private Long activeUsers;

	@JsonProperty("inactive_users")
	private Long inactiveUsers;

	@JsonProperty("blocked_users")
	private Long blockedUsers;

	@JsonProperty("expired_users")
	private Long expiredUsers;

	@JsonProperty("users_by_role")
	private Map<String, Long> usersByRole;

	@JsonProperty("active_users_by_role")
	private Map<String, Long> activeUsersByRole;

	@JsonProperty("total_admins")
	private Long totalAdmins;

	@JsonProperty("total_supervisors")
	private Long totalSupervisors;

	@JsonProperty("total_operators")
	private Long totalOperators;

	@JsonProperty("users_with_workshifts")
	private Long usersWithWorkshifts;

	@JsonProperty("users_who_scanned_palets")
	private Long usersWhoScannedPalets;
}