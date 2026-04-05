package com.transittn.organization_partner.service.impl;

import com.transittn.organization_partner.dto.PartnerDTO;
import com.transittn.organization_partner.entity.Partner;
import com.transittn.organization_partner.entity.PartnerContract;
import com.transittn.organization_partner.enums.ContractStatus;
import com.transittn.organization_partner.enums.ContractType;
import com.transittn.organization_partner.enums.PartnerStatus;
import com.transittn.organization_partner.repository.PartnerContractRepository;
import com.transittn.organization_partner.repository.PartnerRepository;
import com.transittn.organization_partner.service.PartnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PartnerServiceImpl implements PartnerService {

    private final PartnerRepository partnerRepository;
    private final PartnerContractRepository contractRepository;

    private PartnerDTO toDTO(Partner partner) {
        return PartnerDTO.builder()
                .id(partner.getId())
                .name(partner.getName())
                .industrySector(partner.getIndustrySector())
                .partnershipType(partner.getPartnershipType())
                .email(partner.getEmail())
                .phoneNumber(partner.getPhoneNumber())
                .website(partner.getWebsite())
                .logo(partner.getLogo())
                .status(partner.getStatus())
                .build();
    }

    private Partner toEntity(PartnerDTO dto) {
        return Partner.builder()
                .id(dto.getId())
                .name(dto.getName())
                .industrySector(dto.getIndustrySector())
                .partnershipType(dto.getPartnershipType())
                .email(dto.getEmail())
                .phoneNumber(dto.getPhoneNumber())
                .website(dto.getWebsite())
                .logo(dto.getLogo())
                .status(dto.getStatus())
                .build();
    }

    @Override
    public PartnerDTO create(PartnerDTO dto) {
        Partner saved = partnerRepository.save(toEntity(dto));

        // Créer contrat automatique
        PartnerContract contract = PartnerContract.builder()
                .contractType(ContractType.COMMERCIAL)
                .status(ContractStatus.DRAFT)
                .startDate(new Date())
                .endDate(new Date(System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000))
                .description("Auto-generated contract for partner: " + saved.getName())
                .partner(saved)
                .build();
        contractRepository.save(contract);

        return toDTO(saved);
    }

    @Override
    public PartnerDTO update(Long id, PartnerDTO dto) {
        Partner existing = partnerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Partner not found"));
        existing.setName(dto.getName());
        existing.setIndustrySector(dto.getIndustrySector());
        existing.setPartnershipType(dto.getPartnershipType());
        existing.setEmail(dto.getEmail());
        existing.setPhoneNumber(dto.getPhoneNumber());
        existing.setWebsite(dto.getWebsite());
        existing.setLogo(dto.getLogo());
        existing.setStatus(dto.getStatus());
        return toDTO(partnerRepository.save(existing));
    }

    @Override
    public void delete(Long id) {
        partnerRepository.deleteById(id);
    }

    @Override
    public PartnerDTO getById(Long id) {
        return partnerRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Partner not found"));
    }

    @Override
    public List<PartnerDTO> getAll() {
        return partnerRepository.findAll()
                .stream().map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PartnerDTO> getByStatus(PartnerStatus status) {
        return partnerRepository.findByStatus(status)
                .stream().map(this::toDTO)
                .collect(Collectors.toList());
    }
}