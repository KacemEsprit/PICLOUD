package com.example.ticketapp.repository;

import com.example.ticketapp.entity.AITrainingData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AITrainingDataRepository extends JpaRepository<AITrainingData, Long> {
    List<AITrainingData> findByDataType(String dataType);
    long countByDataType(String dataType);
}
