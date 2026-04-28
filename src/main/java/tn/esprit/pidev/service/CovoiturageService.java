package tn.esprit.pidev.service;



import tn.esprit.pidev.entity.Covoiturage;

import tn.esprit.pidev.repository.CovoiturageRepository;

import org.springframework.stereotype.Service;



import java.util.List;



@Service

public class CovoiturageService {



    private final CovoiturageRepository repo;



    public CovoiturageService(CovoiturageRepository repo) {

        this.repo = repo;

    }



    public Covoiturage add(Covoiturage c) {

        return repo.save(c);

    }



    public List<Covoiturage> getAll() {

        return repo.findAll();

    }

    public Covoiturage getById(Long id) {
        return repo.findById(id).orElseThrow(() -> new RuntimeException("Covoiturage non trouvé avec id: " + id));
    }



    public Covoiturage update(Long id, Covoiturage c) {

        Covoiturage old = repo.findById(id).orElseThrow();

        old.setDriverName(c.getDriverName());

        old.setDeparture(c.getDeparture());

        old.setDestination(c.getDestination());

        old.setDate(c.getDate());

        old.setHeureDepart(c.getHeureDepart());

        old.setHeureArrivee(c.getHeureArrivee());

        old.setPrice(c.getPrice());

        old.setAvailableSeats(c.getAvailableSeats());

        old.setVehicle(c.getVehicle());

        old.setStatus(c.getStatus());

        old.setDepartureLat(c.getDepartureLat());

        old.setDepartureLng(c.getDepartureLng());

        old.setDestinationLat(c.getDestinationLat());

        old.setDestinationLng(c.getDestinationLng());

        return repo.save(old);

    }



    public void delete(Long id) {

        repo.deleteById(id);

    }

}

