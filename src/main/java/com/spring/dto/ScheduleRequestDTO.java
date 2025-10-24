package com.spring.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.spring.domain.Schedule;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ScheduleRequestDTO {
//	private Long id; //일정id
	@NotNull(message="회원id는 필수입니다.")
	private Long memberId;//회원id
//	@NotNull(message="동물id는 필수입니다.")
//	private Long petId; //동물의 id
	@NotBlank(message="제목을 입력해야 합니다.")
	private String title; //제목
	//private String content; //내용
	@NotNull(message="날짜를 지정해야 합니다.")
	private LocalDateTime scheduleTime; //시간
	@NotNull(message="반복여부를 선택해야 합니다.")
	private Boolean isRecurring; //반복여부
	private String recurrenceType; //반복주기(daily,weekly,monthly,custom)
	//우선은 주단위로만 (1,2,3주...)
	//private String recurrenceDetail;//반복유형 정보:weekly->mon,wed...sun , custom->90일...
	private LocalDate startDate;//시작일
	private LocalDate endDate;//종료일

    public Schedule toEntity() {
    	Schedule s = new Schedule();
    	s.setMemberId(memberId);
//    	s.setPetId(petId);
    	s.setTitle(title);
    	//s.setContent(content);
    	s.setScheduleTime(scheduleTime);
    	s.setRecurring(isRecurring);
    	s.setRecurrenceType(recurrenceType);
//    	s.setRecurrenceDetail(recurrenceDetail);
    	s.setStartDate(startDate);
    	s.setEndDate(endDate);
        return s;
    }
}
