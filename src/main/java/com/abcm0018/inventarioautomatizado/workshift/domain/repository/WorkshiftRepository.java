package com.abcm0018.inventarioautomatizado.workshift.domain.repository;

import com.abcm0018.inventarioautomatizado.shift.domain.entity.Shift;
import com.abcm0018.inventarioautomatizado.users.domain.entity.User;
import com.abcm0018.inventarioautomatizado.workshift.domain.entity.Workshift;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WorkshiftRepository extends JpaRepository<Workshift, Long> {
    List<Workshift> findByUserAndDate(User user, LocalDate date);
    Optional<Workshift> findByIdAndUser(Long id, String employeeNumber);
    Optional<Workshift> findByUserAndDateAndShift(User user, LocalDate date, Shift shift);
    Optional<Workshift> findByDateAndShift(LocalDate date, Shift shift);
    List<Workshift> findByUser(User user);
    List<Workshift> findByDate(LocalDate date);
}
