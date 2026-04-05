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
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Acronyme is required")
    private String acronyme;

    private String transportType;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email")
    private String email;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    private String website;
    private String logo;

    @NotNull(message = "Type is required")
    private OrgType type;

    @NotNull(message = "Status is required")
    private OrgStatus status;

    @NotNull(message = "Coverage type is required")
    private CoverageType coverageType;
}