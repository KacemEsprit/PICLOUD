package tn.transit.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.transit.backend.entity.Schedule;
import tn.transit.backend.entity.enums.DayType;
import java.util.List;

@Repository
public interface ScheduleRepository
        extends JpaRepository<Schedule, Long> {

    List<Schedule> findByLineId(Long lineId);
    List<Schedule> findByDayType(DayType dayType);
    List<Schedule> findByVehicleId(Long vehicleId);
}