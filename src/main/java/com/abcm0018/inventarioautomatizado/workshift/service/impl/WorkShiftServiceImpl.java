package com.abcm0018.inventarioautomatizado.workshift.service.impl;

import com.abcm0018.inventarioautomatizado.productos.constants.CustomErrorCode;
import com.abcm0018.inventarioautomatizado.shared.config.InventariadoCacheConfig;
import com.abcm0018.inventarioautomatizado.shared.utils.WorkshiftUtils;
import com.abcm0018.inventarioautomatizado.shift.domain.entity.Shift;
import com.abcm0018.inventarioautomatizado.shift.domain.entity.ShiftType;
import com.abcm0018.inventarioautomatizado.shift.domain.repository.ShiftRepository;
import com.abcm0018.inventarioautomatizado.users.domain.entity.Role;
import com.abcm0018.inventarioautomatizado.users.domain.entity.User;
import com.abcm0018.inventarioautomatizado.users.domain.repository.UserRepository;
import com.abcm0018.inventarioautomatizado.workshift.domain.entity.ChangeStatus;
import com.abcm0018.inventarioautomatizado.workshift.domain.entity.ShiftChange;
import com.abcm0018.inventarioautomatizado.workshift.domain.entity.Workshift;
import com.abcm0018.inventarioautomatizado.workshift.domain.repository.ShiftChangeRequestRepository;
import com.abcm0018.inventarioautomatizado.workshift.domain.repository.WorkshiftRepository;
import com.abcm0018.inventarioautomatizado.workshift.exceptions.WorkshiftServiceException;
import com.abcm0018.inventarioautomatizado.workshift.mapper.ShiftChangeMapper;
import com.abcm0018.inventarioautomatizado.workshift.mapper.WorkshiftMapper;
import com.abcm0018.inventarioautomatizado.workshift.service.WorkshiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Random;

@Service
@Transactional
@RequiredArgsConstructor
@CacheConfig(cacheNames = InventariadoCacheConfig.PRODUCT_INFO)
public class WorkShiftServiceImpl implements WorkshiftService {

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            .withResolverStyle(ResolverStyle.STRICT); // evita 31/02 o 29/02 en año no bisiesto

    private final UserRepository userRepository;
    private final ShiftRepository shiftRepository;
    private final WorkshiftRepository workshiftRepository;
    private final ShiftChangeRequestRepository shiftChangeRequestRepository;
    private final Random random = new Random();

    @Override
    @CacheEvict(allEntries = true)
    public void assignWeeklyShifts(String weekStart) {

        LocalDate startDate;
        try {
            startDate = LocalDate.parse(weekStart, formatter);
        } catch (Exception e) {
            throw new WorkshiftServiceException(
                    CustomErrorCode.BAD_REQUEST,
                    "Invalid date format. Please use dd/MM/yyyy",
                    HttpStatus.BAD_REQUEST
            );
        }
        List<User> users = userRepository.findByRoleIn(List.of(Role.OPERATOR, Role.SUPERVISOR));

        if(users.isEmpty()) {
            throw new WorkshiftServiceException(CustomErrorCode.NOT_FOUND, "No users found for OPERATOR or SUPERVISOR roles", HttpStatus.NOT_FOUND);
        }

        List<Shift> shifts = shiftRepository.findAll();

        if(shifts.isEmpty()) {
            throw new WorkshiftServiceException(CustomErrorCode.NOT_FOUND, "No users found for OPERATOR or SUPERVISOR roles", HttpStatus.NOT_FOUND);
        }

        for(int i = 0; i < 7; i++){
            LocalDate currentDate = startDate.plusDays(i);

            for(Shift shift : shifts){

                User randomUser = chooseValidEmployee(users, currentDate);

                Workshift assign = WorkshiftMapper.toEntity(currentDate, randomUser, shift);
                workshiftRepository.save(assign);
            }
        }
    }

    // Asignación de turnos para la semana actual
    @Scheduled(cron = "0 59 23 ? * SUN")
    public void assignShiftForCurrentWeek(){
        // Calculamos el turno para el lunes
        LocalDate currentDate = LocalDate.now()
                .with(DayOfWeek.MONDAY);

        String formattedDate = currentDate.format(formatter);
        assignWeeklyShifts(formattedDate);
    }

    // Asignación de turnos para la siguiente semana
    @Scheduled(cron = "0 59 23 ? * SUN")
    public void assignShiftsForNextWeek(){
        // Calculamos el turno para el lunes de la siguiente semana a partir del lunes de esta
        LocalDate nextMonday = LocalDate.now()
                .with(DayOfWeek.MONDAY)
                .plusWeeks(1); // sumamos una semana

        String formattedDate = nextMonday.format(formatter);
        assignWeeklyShifts(formattedDate);
    }

    // Método para solicitar el cambio de turno
    public ShiftChange requestShiftChange(String employeeNumber, String currentDate, String currentShiftName, String requestedDate, String requestedShiftName, String reason){

        if (!WorkshiftUtils.isEmployeeNumberValid(employeeNumber)) {
            throw new WorkshiftServiceException(CustomErrorCode.BAD_REQUEST, "The employee number to be updated cannot be empty.", HttpStatus.BAD_REQUEST);
        }

        LocalDate currentDay = parseDate(currentDate);

        LocalDate requestedDay = parseDate(requestedDate);

        User user = userRepository.findByEmployeeNumber(employeeNumber)
                .orElseThrow(() -> new WorkshiftServiceException(CustomErrorCode.NOT_FOUND, "User not found", HttpStatus.NOT_FOUND));

        Shift currentShift = shiftRepository.findByShiftType(ShiftType.valueOf(currentShiftName.toUpperCase()))
                .orElseThrow(() -> new WorkshiftServiceException(CustomErrorCode.NOT_FOUND, "Current shift not found", HttpStatus.NOT_FOUND));

        Workshift current = workshiftRepository.findByUserAndDateAndShift(user, currentDay, currentShift)
                .orElseThrow(() -> new WorkshiftServiceException(CustomErrorCode.NOT_FOUND, "Current workshift not found", HttpStatus.NOT_FOUND));

        Shift requestedShift = shiftRepository.findByShiftType(ShiftType.valueOf(requestedShiftName.toUpperCase()))
                .orElseThrow(() -> new WorkshiftServiceException(CustomErrorCode.NOT_FOUND, "Request shift not found", HttpStatus.NOT_FOUND));

        Workshift requested = workshiftRepository.findByDateAndShift(requestedDay, requestedShift)
                .orElseThrow(() -> new WorkshiftServiceException(CustomErrorCode.NOT_FOUND, "Requested workshift not found", HttpStatus.NOT_FOUND));

        if (!current.getUser().equals(user)) {
            throw new WorkshiftServiceException(
                    CustomErrorCode.BAD_REQUEST,
                    "The current workshift does not belong to this user.",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (requested.getUser() != null) {
            throw new WorkshiftServiceException(
                    CustomErrorCode.BAD_REQUEST,
                    "The requested workshift is already assigned to another user.",
                    HttpStatus.BAD_REQUEST
            );
        }

        ShiftChange change = ShiftChangeMapper.toEntity(user, current, requested, reason);

        return shiftChangeRequestRepository.save(change);
    }

    // Método de aprobar/rechazar la solicitud por parte del admin
    public void approveShiftChange(Long requestId, boolean approved){
        ShiftChange change = shiftChangeRequestRepository.findById(requestId)
                .orElseThrow(() -> new WorkshiftServiceException(CustomErrorCode.NOT_FOUND, "Request not found", HttpStatus.NOT_FOUND));

        if(approved){
            change.setStatus(ChangeStatus.APPROVED);
            // Se reasigna el turno: el usuario deja de tener el actual y pasa al nuevo
            Workshift current = change.getCurrentWorkshift();
            Workshift requested = change.getRequestedWorkshift();

            User user = change.getUser();

            current.setUser(null);
            requested.setUser(user);

            workshiftRepository.save(current);
            workshiftRepository.save(requested);
        } else {
            change.setStatus(ChangeStatus.REJECTED);
        }
        change.setUpdatedAt(LocalDateTime.now());
        shiftChangeRequestRepository.save(change);
    }

    private User chooseValidEmployee(List<User> users, LocalDate date) {
        List<User> candidatos = users.stream()
                .filter(u -> workshiftRepository.findByUserAndDate(u, date).size() < 2) // máx 2 turnos por día
                .toList();

        if (candidatos.isEmpty()) {
            return null; // nadie disponible ese día
        }

        return candidatos.get(random.nextInt(candidatos.size()));
    }

    private LocalDate parseDate(String date) {
        try {
            return LocalDate.parse(date, formatter);
        } catch (Exception e) {
            throw new WorkshiftServiceException(
                    CustomErrorCode.BAD_REQUEST,
                    "Invalid date format. Please use dd/MM/yyyy",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

}
