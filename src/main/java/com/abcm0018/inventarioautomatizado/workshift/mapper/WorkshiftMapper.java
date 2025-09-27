package com.abcm0018.inventarioautomatizado.workshift.mapper;

import com.abcm0018.inventarioautomatizado.shift.domain.entity.Shift;
import com.abcm0018.inventarioautomatizado.users.domain.entity.User;
import com.abcm0018.inventarioautomatizado.workshift.domain.entity.Workshift;

import java.time.LocalDate;

public class WorkshiftMapper {

    private WorkshiftMapper(){
        throw new IllegalStateException("Utility class");
    }

    public static Workshift toEntity(LocalDate date, User user, Shift shift){
        return Workshift
                .builder()
                .user(user)
                .shift(shift)
                .date(date)
                .build();
    }
}
