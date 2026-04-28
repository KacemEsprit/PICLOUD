package com.example.ticketapp.repository;

import com.example.ticketapp.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByCovoiturageIdAndStatus(Long covoiturageId, String status);
    List<Reservation> findByClientNameIgnoreCaseAndStatus(String clientName, String status);
}
