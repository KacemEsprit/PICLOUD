package com.transittn.organization_partner.dto;

import com.transittn.organization_partner.enums.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerContractDTO {
    private Long id;

    @NotNull(message = "Le type de contrat est obligatoire")
    private ContractType contractType;

    @NotNull(message = "Le statut est obligatoire")
    private ContractStatus status;

    @NotNull(message = "La date de début est obligatoire")
    private Date startDate;

    @NotNull(message = "La date de fin est obligatoire")
    private Date endDate;

    @Size(max = 500, message = "La description ne doit pas dépasser 500 caractères")
    private String description;

    @NotNull(message = "L'organisation est obligatoire")
    private Long organizationId;

    @NotNull(message = "Le partenaire est obligatoire")
    private Long partnerId;
}