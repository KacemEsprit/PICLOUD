package tn.transit.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.transit.backend.entity.Trip;
import java.util.List;

@Repository
public interface TripRepository
        extends JpaRepository<Trip, Long> {

    List<Trip> findByScheduleId(Long scheduleId);
    List<Trip> findByCompleted(Boolean completed);
}