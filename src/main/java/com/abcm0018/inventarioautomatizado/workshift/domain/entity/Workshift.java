package com.abcm0018.inventarioautomatizado.workshift.domain.entity;

import com.abcm0018.inventarioautomatizado.shift.domain.entity.Shift;
import com.abcm0018.inventarioautomatizado.timesheet.domain.entity.Timesheet;
import com.abcm0018.inventarioautomatizado.users.domain.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "WORKSHIFT")
public class Workshift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id", nullable = false)
    private Shift shift;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "workshift", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Timesheet> timesheets = new ArrayList<>();

    @OneToMany(mappedBy = "requestedWorkshift", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ShiftChange> changeRequests = new ArrayList<>();
}
