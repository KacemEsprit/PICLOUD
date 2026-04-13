package com.transittn.organization_partner.repository;

import com.transittn.organization_partner.entity.PartnerContract;
import com.transittn.organization_partner.enums.ContractStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PartnerContractRepository extends JpaRepository<PartnerContract, Long> {

    List<PartnerContract> findByOrganizationId(Long organizationId);
    List<PartnerContract> findByPartnerId(Long partnerId);
    List<PartnerContract> findByStatus(ContractStatus status);
}
