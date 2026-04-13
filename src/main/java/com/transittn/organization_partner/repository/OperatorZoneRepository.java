package com.transittn.organization_partner.repository;

import com.transittn.organization_partner.entity.OperatorZone;
import com.transittn.organization_partner.enums.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OperatorZoneRepository extends JpaRepository<OperatorZone, Long> {

    List<OperatorZone> findByOrganizationId(Long organizationId);
    List<OperatorZone> findByRegion(Region region);
}
