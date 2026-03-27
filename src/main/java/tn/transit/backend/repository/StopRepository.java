package tn.transit.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.transit.backend.entity.Stop;
import java.util.List;

@Repository
public interface StopRepository
        extends JpaRepository<Stop, Long> {

    List<Stop> findByLineIdOrderBySequenceAsc(Long lineId);
}
