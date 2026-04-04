package com.transittn.organization_partner.dto;

import com.transittn.organization_partner.enums.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationDTO {
    private Long id;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit avoir entre 2 et 100 caractères")
    private String name;

    @NotBlank(message = "L'acronyme est obligatoire")
    private String acronyme;

    @NotBlank(message = "Le type de transport est obligatoire")
    private String transportType;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Email invalide")
    private String email;

    @NotBlank(message = "Le téléphone est obligatoire")
    @Pattern(regexp = "^\\+216[0-9]{8}$", message = "Numéro tunisien invalide ex: +21671000000")
    private String phoneNumber;

    private String website;
    private String logo;

    @NotNull(message = "Le type est obligatoire")
    private OrgType type;

    @NotNull(message = "Le statut est obligatoire")
    private OrgStatus status;

    @NotNull(message = "Le type de couverture est obligatoire")
    private CoverageType coverageType;
}