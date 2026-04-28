package tn.esprit.pidev.service;

import tn.esprit.pidev.entity.Reservation;
import tn.esprit.pidev.repository.ReservationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReservationService {

    private final ReservationRepository repo;

    public ReservationService(ReservationRepository repo) {
        this.repo = repo;
    }

    public Reservation add(Reservation r) {
        return repo.save(r);
    }

    public List<Reservation> getAll() {
        return repo.findAll();
    }

    public Reservation update(Long id, Reservation r) {
        Reservation old = repo.findById(id).orElseThrow();
        old.setClientName(r.getClientName());
        old.setPhone(r.getPhone());
        old.setEmail(r.getEmail());
        old.setSeatsReserved(r.getSeatsReserved());
        old.setBookingDate(r.getBookingDate());
        old.setStatus(r.getStatus());
        old.setCovoiturageId(r.getCovoiturageId());
        old.setClientLat(r.getClientLat());
        old.setClientLng(r.getClientLng());
        old.setClientAddress(r.getClientAddress());
        old.setDisplacementRequested(r.getDisplacementRequested());
        old.setDisplacementPrice(r.getDisplacementPrice());
        return repo.save(old);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}
