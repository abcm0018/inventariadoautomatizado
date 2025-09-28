package com.abcm0018.inventarioautomatizado.workshift.mapper;

import com.abcm0018.inventarioautomatizado.users.domain.entity.User;
import com.abcm0018.inventarioautomatizado.workshift.domain.entity.ChangeStatus;
import com.abcm0018.inventarioautomatizado.workshift.domain.entity.WorkshiftChange;
import com.abcm0018.inventarioautomatizado.workshift.domain.entity.Workshift;

import java.time.LocalDateTime;

public class WorkshiftChangeMapper {
    private WorkshiftChangeMapper(){
        throw new IllegalStateException("Utility class");
    }

    public static WorkshiftChange toEntity(User user, Workshift current, Workshift requested, String reason){
        return WorkshiftChange
                .builder()
                .user(user)
                .currentWorkshift(current)
                .reason(reason)
                .status(ChangeStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
