package tn.transit.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.transit.backend.entity.Vehicle;
import tn.transit.backend.entity.enums.VehicleStatus;
import tn.transit.backend.entity.enums.VehicleType;
import java.util.List;

@Repository
public interface VehicleRepository
        extends JpaRepository<Vehicle, Long> {

    List<Vehicle> findByStatus(VehicleStatus status);
    List<Vehicle> findByType(VehicleType type);
}