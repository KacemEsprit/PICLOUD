package com.transittn.organization_partner.repository;

import com.transittn.organization_partner.entity.Organization;
import com.transittn.organization_partner.enums.OrgStatus;
import com.transittn.organization_partner.enums.CoverageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    List<Organization> findByStatus(OrgStatus status);
    List<Organization> findByCoverageType(CoverageType coverageType);
    boolean existsByName(String name);
}
