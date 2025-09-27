package com.abcm0018.inventarioautomatizado.timesheet.domain.entity;

import com.abcm0018.inventarioautomatizado.users.domain.entity.User;
import com.abcm0018.inventarioautomatizado.shift.domain.entity.Shift;
import com.abcm0018.inventarioautomatizado.workshift.domain.entity.Workshift;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "TIMESHEET")
public class Timesheet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "check_in_at", nullable = false)
    private LocalDateTime checkInAt;

    @Column(name = "check_out_at", nullable = false)
    private LocalDateTime checkOutAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_number", referencedColumnName = "employee_number", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id", referencedColumnName = "id")
    private Shift shift;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workshift_id", referencedColumnName = "id")
    private Workshift workshift;

}
