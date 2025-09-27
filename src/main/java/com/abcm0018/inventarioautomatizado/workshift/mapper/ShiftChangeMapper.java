package com.abcm0018.inventarioautomatizado.workshift.mapper;

import com.abcm0018.inventarioautomatizado.users.domain.entity.User;
import com.abcm0018.inventarioautomatizado.workshift.domain.entity.ChangeStatus;
import com.abcm0018.inventarioautomatizado.workshift.domain.entity.ShiftChange;
import com.abcm0018.inventarioautomatizado.workshift.domain.entity.Workshift;

import java.time.LocalDateTime;

public class ShiftChangeMapper {
    private ShiftChangeMapper(){
        throw new IllegalStateException("Utility class");
    }

    public static ShiftChange toEntity(User user, Workshift current, Workshift requested, String reason){
        return ShiftChange
                .builder()
                .user(user)
                .currentWorkshift(current)
                .requestedWorkshift(requested)
                .reason(reason)
                .status(ChangeStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
