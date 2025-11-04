package com.spring.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.spring.domain.ScheduleInstance;

@Repository
public interface ScheduleInstanceRepository extends JpaRepository<ScheduleInstance, Long> {
    void deleteAllByScheduleId(Long scheduleId);
    List<ScheduleInstance> findAllByScheduleId(Long scheduleId);
}
