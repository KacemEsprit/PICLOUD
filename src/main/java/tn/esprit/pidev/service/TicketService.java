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

    public String seedTestData() {
        // Clear existing tickets to avoid duplicates
        repo.deleteAll();

        // Create test tickets with proper data
        Ticket[] tickets = {
            createTicket("Standard", 0.5, "Bus urbain Tunis", "1 trajet", TransportType.BUS, 100, "Tunis Centre", "Ezzahra", "08:00"),
            createTicket("Standard", 1.5, "Bus interurbain Tunis-Sfax", "1 trajet", TransportType.BUS, 50, "Tunis", "Sfax", "06:30"),
            createTicket("Standard", 0.8, "Métro léger TGM", "1 trajet", TransportType.METRO, 200, "Tunis", "La Marsa", "07:00"),
            createTicket("Standard", 1.2, "Train banlieue", "1 trajet", TransportType.TRAIN, 150, "Tunis", "Rades", "09:00"),
            createTicket("Premium", 18.0, "Train Tunis-Sfax", "1 trajet", TransportType.TRAIN, 75, "Tunis", "Sfax", "10:15"),
            createTicket("Abonnement", 15.0, "Bus étudiant mensuel", "30 jours", TransportType.BUS, 300, "Tunis", "Zone Urbaine", "00:00"),
            createTicket("Réduit", 2.5, "Train étudiant", "1 trajet", TransportType.TRAIN, 120, "Tunis", "Bizerte", "11:30"),
            createTicket("Abonnement", 20.0, "Métro abonnement", "30 jours", TransportType.METRO, 250, "Tunis", "Suburbs", "00:00"),
            createTicket("VIP", 5.0, "Bus VIP climatisé", "1 trajet", TransportType.BUS, 40, "Tunis", "Nabeul", "08:30"),
            createTicket("Standard", 12.0, "Louage collectif", "1 trajet", TransportType.LOUAGE, 8, "Tunis", "Kairouan", "07:00"),
            createTicket("Standard", 7.5, "Bus Tunis-Nabeul", "1 trajet", TransportType.BUS, 60, "Tunis", "Nabeul", "09:30"),
            createTicket("Standard", 4.0, "Train régional", "1 trajet", TransportType.TRAIN, 90, "Tunis", "Mahdia", "13:00"),
        };

        repo.saveAll(java.util.Arrays.asList(tickets));
        return "Successfully seeded " + tickets.length + " test tickets";
    }

    private Ticket createTicket(String type, double price, String description, String validity,
                               TransportType transportType, int quantity, String departure,
                               String destination, String departureTime) {
        Ticket t = new Ticket();
        t.setType(type);
        t.setPrice(price);
        t.setDescription(description);
        t.setValidity(validity);
        t.setTransportType(transportType);
        t.setQuantiteDisponible(quantity);
        t.setLieuDepart(departure);
        t.setDestination(destination);
        t.setHeureDepart(departureTime);
        return t;
    }
}
