package com.abcm0018.inventarioautomatizado.workshift.domain.repository;

import com.abcm0018.inventarioautomatizado.workshift.domain.entity.WorkshiftChange;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkshiftChangeRepository extends JpaRepository<WorkshiftChange, Long> {
}
