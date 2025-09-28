package com.abcm0018.inventarioautomatizado.users.domain.entity;

import com.abcm0018.inventarioautomatizado.timesheet.domain.entity.Timesheet;
import com.abcm0018.inventarioautomatizado.workshift.domain.entity.WorkshiftChange;
import com.abcm0018.inventarioautomatizado.workshift.domain.entity.Workshift;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "USER")
public class User implements UserDetails, Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_number", nullable = false, unique = true)
    private String employeeNumber;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "surname", nullable = false)
    private String surname;
    @Column(name = "email", nullable = false, unique = true)
    private String email;
    @Column(name = "password", nullable = false)
    private String password;
    @Column(name = "job_position", nullable = false)
    private String jobPosition;
    @Column(name = "active", nullable = false)
    private boolean active;
    @Column(name = "blocked", nullable = false)
    private boolean blocked;
    @Column(name = "expirated", nullable = false)
    private boolean expirated;
    @Column(name = "registration_date", nullable = false)
    private LocalDate registrationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Workshift> workshifts = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Timesheet> timesheets = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<WorkshiftChange> shiftsChanges = new ArrayList<>();

    private String getFullName (){
        return name + " " + surname;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(){
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getUsername() {
        return employeeNumber;
    }

    @Override
    public boolean isAccountNonExpired(){
        return !isExpirated();
    }

    @Override
    public boolean isAccountNonLocked(){
        return !isBlocked();
    }

    @Override
    public boolean isCredentialsNonExpired(){
        return true;
    }

    @Override
    public boolean isEnabled(){
        return isActive();
    }
}
