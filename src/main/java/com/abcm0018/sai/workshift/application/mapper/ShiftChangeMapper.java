package com.abcm0018.sai.workshift.application.mapper;

import com.abcm0018.sai.users.domain.entity.User;
import com.abcm0018.sai.workshift.domain.enums.SwapRequestStatus;
import com.abcm0018.sai.workshift.domain.entity.WorkshiftSwapRequest;
import com.abcm0018.sai.workshift.domain.entity.Workshift;

import java.time.LocalDateTime;

public class ShiftChangeMapper {
    private ShiftChangeMapper(){
        throw new IllegalStateException("Utility class");
    }

    public static WorkshiftSwapRequest toEntity(User user, Workshift current, Workshift requested, String reason){
        return WorkshiftSwapRequest
                .builder()
                .user(user)
                .currentWorkshift(current)
                .requestedWorkshift(requested)
                .reason(reason)
                .status(SwapRequestStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
