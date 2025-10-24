package com.spring.dto;


import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.domain.Symptom;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SymptomRequestDTO {
	@NotNull(message="회원id를 입력해야 합니다.")
	private Long memberId;
	@NotNull(message="동물id를 입력해야 합니다.")
    private Long petId;
	@NotNull(message="날짜를 지정해야 합니다.")
    private LocalDateTime symptomDate;
    private String description;
    private String selectedSymptomIds; // 선택된 증상 ID들
    private List<Long> suspectedDiseaseIds; //관련질병id
    
    public Symptom toEntity() {
        Symptom s = new Symptom();
        s.setMemberId(memberId);
        s.setPetId(petId);
        s.setSymptomDate(symptomDate);
        s.setDescription(description);
        
        ObjectMapper mapper = new ObjectMapper();
        
        try {
            String selectedJson = mapper.writeValueAsString(selectedSymptomIds);
            s.setSelectedSymptomIds(selectedJson);
        } catch (Exception e) {
            s.setSelectedSymptomIds("[]");
        }
        
        try {
            String suspectedJson = mapper.writeValueAsString(suspectedDiseaseIds);
            s.setSuspectedDiseaseIds(suspectedJson);
        } catch (Exception e) {
            s.setSuspectedDiseaseIds("[]");
        }

        return s;
    }
    
}