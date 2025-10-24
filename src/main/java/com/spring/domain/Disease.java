package com.spring.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "disease")
public class Disease {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                  // 질병 ID
    private String name;              // 질병명 (예: 장염)
    private String description;       // 질병 설명
    @Column(name = "related_symptom_ids", length = 1000)
    private String relatedSymptomIds; // 연관 증상 ID 목록 json형태로저장
}