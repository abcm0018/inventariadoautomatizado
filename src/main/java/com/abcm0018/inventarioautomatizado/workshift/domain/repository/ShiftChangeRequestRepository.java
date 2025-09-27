package com.abcm0018.inventarioautomatizado.workshift.domain.repository;

import com.abcm0018.inventarioautomatizado.workshift.domain.entity.ShiftChange;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShiftChangeRequestRepository extends JpaRepository<ShiftChange, Long> {
}
