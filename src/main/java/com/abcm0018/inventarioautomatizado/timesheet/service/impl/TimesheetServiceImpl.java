package com.abcm0018.inventarioautomatizado.timesheet.service.impl;

import com.abcm0018.inventarioautomatizado.productos.constants.CustomErrorCode;
import com.abcm0018.inventarioautomatizado.shared.config.InventariadoCacheConfig;
import com.abcm0018.inventarioautomatizado.shared.utils.TimesheetUtils;
import com.abcm0018.inventarioautomatizado.timesheet.domain.entity.Timesheet;
import com.abcm0018.inventarioautomatizado.timesheet.domain.repository.TimesheetRepository;
import com.abcm0018.inventarioautomatizado.timesheet.dtos.TimesheetRequest;
import com.abcm0018.inventarioautomatizado.timesheet.dtos.TimesheetResponseDTO;
import com.abcm0018.inventarioautomatizado.timesheet.exceptions.TimesheetServiceException;
import com.abcm0018.inventarioautomatizado.timesheet.mapper.TimesheetMapper;
import com.abcm0018.inventarioautomatizado.timesheet.service.TimesheetService;
import com.abcm0018.inventarioautomatizado.users.domain.entity.User;
import com.abcm0018.inventarioautomatizado.users.domain.repository.UserRepository;
import com.abcm0018.inventarioautomatizado.shift.domain.entity.Shift;
import com.abcm0018.inventarioautomatizado.shift.domain.entity.ShiftType;
import com.abcm0018.inventarioautomatizado.shift.domain.repository.ShiftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@CacheConfig(cacheNames = InventariadoCacheConfig.PRODUCT_INFO)
public class TimesheetServiceImpl implements TimesheetService {

    private final TimesheetRepository timesheetRepository;
    private final UserRepository userRepository;
    private final ShiftRepository shiftRepository;

    @Override
    public TimesheetResponseDTO updateTimesheet(String employeeNumber, String shift, TimesheetRequest request) {
        Timesheet timesheet = getTimesheetByEmployeeAndShift(employeeNumber, shift);

        Timesheet timesheetToUpdate = TimesheetMapper.toEntity(request, timesheet.getUser(), timesheet.getShift());

        timesheetToUpdate.setId(timesheet.getId());
        timesheetToUpdate.setUser(timesheet.getUser());

        Timesheet udpatedTimesheet = timesheetRepository.save(timesheetToUpdate);
        return TimesheetMapper.toDTO(udpatedTimesheet);
    }

    @Override
    public void deleteTimesheet(String employeeNumber, String shift) {
        Timesheet timesheet = getTimesheetByEmployeeAndShift(employeeNumber, shift);
        timesheetRepository.delete(timesheet);
    }

    @Override
    public TimesheetResponseDTO getTimesheet(String employeeNumber, String shift) {
        Timesheet info = getTimesheetByEmployeeAndShift(employeeNumber, shift);
        return TimesheetMapper.toDTO(info);
    }

    @Override
    public List<TimesheetResponseDTO> getAllTimesheets() {
        List<Timesheet> timesheetList = timesheetRepository.findAll();
        System.out.println(timesheetRepository.findAll().size());
        return  TimesheetMapper.toDTOList(timesheetList);
    }

    private User getUserByValidEmployeeNumber(String employeeNumber) {
        if (!TimesheetUtils.isEmployeeNumberValid(employeeNumber)) {
            throw new TimesheetServiceException(
                    CustomErrorCode.BAD_REQUEST,
                    "The employee number does not have a valid format.",
                    HttpStatus.BAD_REQUEST
            );
        }

        return userRepository.findByEmployeeNumber(employeeNumber)
                .orElseThrow(() -> new TimesheetServiceException(
                        CustomErrorCode.NOT_FOUND,
                        "User not found: " + employeeNumber,
                        HttpStatus.NOT_FOUND
                ));
    }

    private Timesheet getTimesheetByEmployeeAndShift(String employeeNumber, String shift) {
        User user = getUserByValidEmployeeNumber(employeeNumber);

        Shift existShift = shiftRepository.findByShiftType(ShiftType.valueOf(shift.toUpperCase()))
                .orElseThrow(() -> new TimesheetServiceException(
                        CustomErrorCode.NOT_FOUND,
                        "Shift not found: " + shift,
                        HttpStatus.NOT_FOUND
                ));

        return timesheetRepository.findByUserAndShift(user, existShift)
                .orElseThrow(() -> new TimesheetServiceException(
                        CustomErrorCode.NOT_FOUND,
                        "Timesheet not found for user: " + user.getEmployeeNumber() + " with shift: " + shift,
                        HttpStatus.NOT_FOUND
                ));
    }


}
