package com.transittn.organization_partner.service.impl;

import com.transittn.organization_partner.dto.OrganizationDTO;
import com.transittn.organization_partner.entity.Organization;
import com.transittn.organization_partner.enums.CoverageType;
import com.transittn.organization_partner.enums.OrgStatus;
import com.transittn.organization_partner.repository.OrganizationRepository;
import com.transittn.organization_partner.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationRepository organizationRepository;

    private OrganizationDTO toDTO(Organization org) {
        return OrganizationDTO.builder()
                .id(org.getId())
                .name(org.getName())
                .acronyme(org.getAcronyme())
                .email(org.getEmail())
                .phoneNumber(org.getPhoneNumber())
                .website(org.getWebsite())
                .logo(org.getLogo())
                .type(org.getType())
                .status(org.getStatus())
                .coverageType(org.getCoverageType())
                .build();
    }

    private Organization toEntity(OrganizationDTO dto) {
        return Organization.builder()
                .id(dto.getId())
                .name(dto.getName())
                .acronyme(dto.getAcronyme())
                .email(dto.getEmail())
                .phoneNumber(dto.getPhoneNumber())
                .website(dto.getWebsite())
                .logo(dto.getLogo())
                .type(dto.getType())
                .status(dto.getStatus())
                .coverageType(dto.getCoverageType())
                .build();
    }

    @Override
    public OrganizationDTO create(OrganizationDTO dto) {
        Organization org = toEntity(dto);
        return toDTO(organizationRepository.save(org));
    }

    @Override
    public OrganizationDTO update(Long id, OrganizationDTO dto) {
        Organization existing = organizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        existing.setName(dto.getName());
        existing.setAcronyme(dto.getAcronyme());
        existing.setEmail(dto.getEmail());
        existing.setPhoneNumber(dto.getPhoneNumber());
        existing.setWebsite(dto.getWebsite());
        existing.setLogo(dto.getLogo());
        existing.setType(dto.getType());
        existing.setStatus(dto.getStatus());
        existing.setCoverageType(dto.getCoverageType());
        return toDTO(organizationRepository.save(existing));
    }

    @Override
    public void delete(Long id) {
        organizationRepository.deleteById(id);
    }

    @Override
    public OrganizationDTO getById(Long id) {
        return organizationRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Organization not found"));
    }

    @Override
    public List<OrganizationDTO> getAll() {
        return organizationRepository.findAll()
                .stream().map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrganizationDTO> getByStatus(OrgStatus status) {
        return organizationRepository.findByStatus(status)
                .stream().map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrganizationDTO> getByCoverageType(CoverageType coverageType) {
        return organizationRepository.findByCoverageType(coverageType)
                .stream().map(this::toDTO)
                .collect(Collectors.toList());
    }
}
