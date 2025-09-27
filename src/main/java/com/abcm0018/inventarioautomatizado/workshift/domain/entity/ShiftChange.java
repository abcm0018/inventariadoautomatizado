package com.abcm0018.inventarioautomatizado.workshift.domain.entity;

import com.abcm0018.inventarioautomatizado.users.domain.entity.User;
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
@Table(name = "SHIFTCHANGE")
public class ShiftChange {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String reason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Usuario que solicita el cambio
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    private ChangeStatus status;

    // Turno actual asignado
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_workshift_id", nullable = false)
    private Workshift currentWorkshift;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_workshift_id", nullable = false)
    private Workshift requestedWorkshift;
}


