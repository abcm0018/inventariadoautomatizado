package com.abcm0018.inventarioautomatizado.workshift.service.impl;

import com.abcm0018.inventarioautomatizado.productos.constants.CustomErrorCode;
import com.abcm0018.inventarioautomatizado.shared.config.InventariadoCacheConfig;
import com.abcm0018.inventarioautomatizado.shift.domain.entity.Shift;
import com.abcm0018.inventarioautomatizado.shift.domain.entity.ShiftType;
import com.abcm0018.inventarioautomatizado.shift.domain.repository.ShiftRepository;
import com.abcm0018.inventarioautomatizado.users.domain.entity.Role;
import com.abcm0018.inventarioautomatizado.users.domain.entity.User;
import com.abcm0018.inventarioautomatizado.users.domain.repository.UserRepository;
import com.abcm0018.inventarioautomatizado.workshift.domain.entity.ChangeStatus;
import com.abcm0018.inventarioautomatizado.workshift.domain.entity.WorkshiftChange;
import com.abcm0018.inventarioautomatizado.workshift.domain.entity.Workshift;
import com.abcm0018.inventarioautomatizado.workshift.domain.repository.WorkshiftChangeRepository;
import com.abcm0018.inventarioautomatizado.workshift.domain.repository.WorkshiftRepository;
import com.abcm0018.inventarioautomatizado.workshift.dtos.ShiftChangeRequest;
import com.abcm0018.inventarioautomatizado.workshift.exceptions.WorkshiftServiceException;
import com.abcm0018.inventarioautomatizado.workshift.mapper.WorkshiftChangeMapper;
import com.abcm0018.inventarioautomatizado.workshift.mapper.WorkshiftMapper;
import com.abcm0018.inventarioautomatizado.workshift.service.WorkshiftService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@CacheConfig(cacheNames = InventariadoCacheConfig.PRODUCT_INFO)
public class WorkShiftServiceImpl implements WorkshiftService {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT); // evita 31/02 o 29/02 en año no bisiesto

    private static final Integer SUCCESS = 1;
    private static final Integer ERROR = 0;

    private final UserRepository userRepository;
    private final ShiftRepository shiftRepository;
    private final WorkshiftRepository workshiftRepository;
    private final WorkshiftChangeRepository shiftChangeRequestRepository;
    private final Random random = new Random();
    private final WorkshiftChangeRepository workshiftChangeRepository;

    @Override
    @CacheEvict(allEntries = true)
    public void assignWeeklyShifts(LocalDate weekStart) {


//        try {
//            startDate = LocalDate.parse(weekStart, formatter);
//        } catch (Exception e) {
//            throw new WorkshiftServiceException(
//                    CustomErrorCode.BAD_REQUEST,
//                    "Invalid date format. Please use dd/MM/yyyy",
//                    HttpStatus.BAD_REQUEST
//            );
//        }
        List<User> users = userRepository.findByRoleIn(List.of(Role.OPERATOR, Role.SUPERVISOR));

        if(users.isEmpty()) {
            throw new WorkshiftServiceException(CustomErrorCode.NOT_FOUND, "No users found for OPERATOR or SUPERVISOR roles", HttpStatus.NOT_FOUND);
        }

        List<Shift> shifts = shiftRepository.findAll();

        if(shifts.isEmpty()) {
            throw new WorkshiftServiceException(CustomErrorCode.NOT_FOUND, "No users found for OPERATOR or SUPERVISOR roles", HttpStatus.NOT_FOUND);
        }

        for (User user : users) {
            //Shift fixedShift = chooseFixedShiftForUser(user, shifts);
            Shift fixedShift = chooseFixedShiftForUser(shifts);

            int daysToAssign = 5;
            LocalDate startDate = fixedShift.getShiftType() == ShiftType.NIGHT
                    ? weekStart.minusDays(1) // domingo → jueves
                    : weekStart;             // lunes → viernes


            for(int i = 0; i < daysToAssign; i++) {
                LocalDate currentDate = startDate.plusDays(i);

                // Evitar duplicados
                if (workshiftRepository.findByUserAndDate(user, currentDate).isEmpty()) {
                    Workshift assign = WorkshiftMapper.toEntity(currentDate, user, fixedShift);
                    workshiftRepository.save(assign);
                }
            }
        }
    }

    // Asignación de turnos para la semana actual
    @Scheduled(cron = "0 59 23 ? * SUN")
    public void assignShiftForCurrentWeek(){
        // Calculamos el turno para el lunes
        LocalDate currentDate = LocalDate.now()
                .with(DayOfWeek.MONDAY);

        //String formattedDate = currentDate.format(formatter);
        assignWeeklyShifts(currentDate);
    }

    // Asignación de turnos para la siguiente semana
    @Scheduled(cron = "0 59 23 ? * SUN")
    public void assignShiftsForNextWeek(){
        // Calculamos el turno para el lunes de la siguiente semana a partir del lunes de esta
        LocalDate nextMonday = LocalDate.now()
                .with(DayOfWeek.MONDAY)
                .plusWeeks(1); // sumamos una semana

        //String formattedDate = nextMonday.format(formatter);
        assignWeeklyShifts(nextMonday);
    }

    // Método para solicitar el cambio de turno
    public Integer requestWorkshiftChange(ShiftChangeRequest request){

        Workshift current = workshiftRepository.findByIdAndUser(request.getWorkshiftId(), request.getEmployeeNumber())
                .orElseThrow(() -> new WorkshiftServiceException(CustomErrorCode.NOT_FOUND, "Current workshift not found", HttpStatus.NOT_FOUND));

        Shift shift = shiftRepository.findByShiftType(ShiftType.valueOf(request.getNewShift().toUpperCase()))
                .orElseThrow(() -> new WorkshiftServiceException(CustomErrorCode.NOT_FOUND, "Shift not found", HttpStatus.NOT_FOUND));

        LocalDate newWorkshiftDate = parseDate(request.getNewWorkshiftDate());

        Workshift requestWorkshift = WorkshiftMapper.toEntity(newWorkshiftDate, current.getUser(), shift);

        WorkshiftChange change = WorkshiftChangeMapper.toEntity(current.getUser(), current, requestWorkshift, request.getReason());


        return workshiftChangeRepository.save(change) == null ? ERROR : SUCCESS;
    }

    // Método de aprobar/rechazar la solicitud por parte del admin
    public void approveShiftChange(Long requestId, boolean approved){
        WorkshiftChange change = shiftChangeRequestRepository.findById(requestId)
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

//    private Shift chooseFixedShiftForUser(User user, List<Shift> shifts){
//        return shifts.get(random.nextInt(shifts.size()));
//    }

    private int morningCount = 0;
    private int afternoonCount = 0;
    private int nightCount = 0;

    private Shift chooseFixedShiftForUser(List<Shift> shifts) {
        // Encuentra el turno con menos usuarios
        Shift chosenShift = shifts.get(0);
        int minCount = morningCount;

        for (Shift shift : shifts) {
            int count = 0;
            switch (shift.getShiftType()) {
                case MORNING -> count = morningCount;
                case AFTERNOON -> count = afternoonCount;
                case NIGHT -> count = nightCount;
            }

            if (count < minCount) {
                minCount = count;
                chosenShift = shift;
            }
        }

        // Incrementar contador correspondiente
        switch (chosenShift.getShiftType()) {
            case MORNING -> morningCount++;
            case AFTERNOON -> afternoonCount++;
            case NIGHT -> nightCount++;
        }

        return chosenShift;
    }

    private LocalDate parseDate(String date) {
        try {
            log.info("Parsing date: '{}'", date);
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
