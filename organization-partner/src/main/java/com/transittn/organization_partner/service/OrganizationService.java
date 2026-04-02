package com.transittn.organization_partner.service;

import com.transittn.organization_partner.dto.OrganizationDTO;
import com.transittn.organization_partner.enums.CoverageType;
import com.transittn.organization_partner.enums.OrgStatus;
import java.util.List;

public interface OrganizationService {
    OrganizationDTO create(OrganizationDTO dto);
    OrganizationDTO update(Long id, OrganizationDTO dto);
    void delete(Long id);
    OrganizationDTO getById(Long id);
    List<OrganizationDTO> getAll();
    List<OrganizationDTO> getByStatus(OrgStatus status);
    List<OrganizationDTO> getByCoverageType(CoverageType coverageType);
}
