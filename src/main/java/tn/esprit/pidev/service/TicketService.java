package tn.esprit.pidev.service;

import tn.esprit.pidev.entity.Ticket;
import tn.esprit.pidev.entity.TransportType;
import tn.esprit.pidev.repository.TicketRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketService {

    private final TicketRepository repo;

    public TicketService(TicketRepository repo) {
        this.repo = repo;
    }

    public Ticket add(Ticket t) {
        t.setDisponible(t.getQuantiteDisponible() > 0);
        return repo.save(t);
    }

    public List<Ticket> getAll() {
        return repo.findAll();
    }

    public List<Ticket> getDisponibles() {
        return repo.findByDisponibleTrue();
    }

    public List<Ticket> getByTransportType(TransportType transportType) {
        return repo.findByTransportType(transportType);
    }

    public List<Ticket> getDisponiblesByTransportType(TransportType transportType) {
        return repo.findByTransportTypeAndDisponibleTrue(transportType);
    }

    public Ticket update(Long id, Ticket t) {
        Ticket old = repo.findById(id).orElseThrow(() ->
                new RuntimeException("Ticket non trouvé avec id: " + id));
        old.setType(t.getType());
        old.setPrice(t.getPrice());
        old.setDescription(t.getDescription());
        old.setValidity(t.getValidity());
        old.setTransportType(t.getTransportType());
        old.setQuantiteDisponible(t.getQuantiteDisponible());
        old.setLieuDepart(t.getLieuDepart());
        old.setDestination(t.getDestination());
        old.setHeureDepart(t.getHeureDepart());
        return repo.save(old);
    }

    public Ticket acheterTicket(Long id) {
        Ticket ticket = repo.findById(id).orElseThrow(() ->
                new RuntimeException("Ticket non trouvé avec id: " + id));
        if (!ticket.isDisponible() || ticket.getQuantiteDisponible() <= 0) {
            throw new RuntimeException("Ticket épuisé — plus de tickets disponibles");
        }
        ticket.setQuantiteDisponible(ticket.getQuantiteDisponible() - 1);
        return repo.save(ticket);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}
