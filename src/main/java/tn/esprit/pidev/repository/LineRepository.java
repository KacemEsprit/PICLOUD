package tn.transit.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.transit.backend.entity.Line;
import tn.transit.backend.entity.enums.LineStatus;
import tn.transit.backend.entity.enums.TransportMode;
import java.util.List;

@Repository
public interface LineRepository
        extends JpaRepository<Line, Long> {

    List<Line> findByStatus(LineStatus status);
    List<Line> findByMode(TransportMode mode);
}