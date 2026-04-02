package com.transittn.organization_partner.repository;

import com.transittn.organization_partner.entity.Partner;
import com.transittn.organization_partner.enums.PartnerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PartnerRepository extends JpaRepository<Partner, Long> {

    List<Partner> findByStatus(PartnerStatus status);
    boolean existsByEmail(String email);
}
