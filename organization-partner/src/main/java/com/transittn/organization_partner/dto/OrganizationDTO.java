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

    @NotBlank(message = "Name is required")
    private String name;

    private String acronyme;

    @Email(message = "Invalid email format")
    private String email;

    private String phoneNumber;
    private String website;
    private String logo;

    @NotNull(message = "Type is required")
    private OrgType type;

    @NotNull(message = "Status is required")
    private OrgStatus status;

    private CoverageType coverageType;
}