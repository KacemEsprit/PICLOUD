package com.example.ticketapp.repository;



import com.example.ticketapp.entity.Covoiturage;

import org.springframework.data.jpa.repository.JpaRepository;



public interface CovoiturageRepository extends JpaRepository<Covoiturage, Long> {

}

