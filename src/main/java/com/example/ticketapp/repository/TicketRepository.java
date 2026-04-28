package com.example.ticketapp.repository;

import com.example.ticketapp.entity.Ticket;
import com.example.ticketapp.entity.TransportType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByTransportType(TransportType transportType);

    List<Ticket> findByDisponibleTrue();

    List<Ticket> findByTransportTypeAndDisponibleTrue(TransportType transportType);
}
