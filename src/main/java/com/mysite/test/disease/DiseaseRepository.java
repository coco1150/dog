package com.mysite.test.disease;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DiseaseRepository extends JpaRepository<Disease, Long> {
    //relatedSymptomIds 컬럼에 특정 증상 ID 문자열이 포함되어 있는 질병 검색
	@Query("SELECT d FROM Disease d WHERE :symptomId MEMBER OF d.relatedSymptomIds")
	List<Disease> findBySymptomIds(@Param("symptomId") Long symptomId);



}