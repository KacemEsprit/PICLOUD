package tn.transit.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.transit.backend.entity.Trip;
import tn.transit.backend.repository.TripRepository;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository repository;

    public List<Trip> getAll() {
        return repository.findAll();
    }

    public Optional<Trip> getById(Long id) {
        return repository.findById(id);
    }

    public Trip create(Trip trip) {
        return repository.save(trip);
    }

    public Trip update(Long id, Trip trip) {
        trip.setId(id);
        return repository.save(trip);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public List<Trip> getBySchedule(Long scheduleId) {
        return repository.findByScheduleId(scheduleId);
    }
}