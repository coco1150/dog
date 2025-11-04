package com.spring.domain;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "schedule_instance")
public class ScheduleInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

	// 어떤 Schedule에서 생성된 인스턴스인지 연결
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @Column(nullable = false)
    private LocalDateTime occurrenceTime; // 실제 발생 시간

    @Column(nullable = false)
    private boolean completed = false; // 완료 여부 (향후 사용)
    
    public ScheduleInstance(Schedule schedule, LocalDateTime occurrenceTime) {
        this.schedule = schedule;
        this.occurrenceTime = occurrenceTime;
    }
}
