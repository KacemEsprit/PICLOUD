package tn.esprit.pidev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.pidev.entity.Stop;
import java.util.List;

@Repository
public interface StopRepository
        extends JpaRepository<Stop, Long> {

    List<Stop> findByLineIdOrderBySequenceAsc(Long lineId);
}
