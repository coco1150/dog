package com.spring.dto;


import java.time.LocalDate;
import java.time.LocalDateTime;

import com.spring.domain.Schedule;
import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class ScheduleResponseDTO {

    private Long id;
    private Long memberId;
    //private Long petId;
    private String title;
    //private String content;
    private LocalDateTime scheduleTime;
    private boolean recurring;
    private String recurrenceType;
   // private String recurrenceDetail;
    private LocalDate startDate;
    private LocalDate endDate;

    // Entity > ResponseDTO 변환 메서드
    public static ScheduleResponseDTO fromEntity(Schedule schedule) {
        return new ScheduleResponseDTO(
                schedule.getId(),
                schedule.getMemberId(),
//                schedule.getPetId(),
                schedule.getTitle(),
                //schedule.getContent(),
                schedule.getScheduleTime(),
                schedule.isRecurring(),
                schedule.getRecurrenceType(),
//                schedule.getRecurrenceDetail(),
                schedule.getStartDate(),
                schedule.getEndDate()
        );
    }

}
