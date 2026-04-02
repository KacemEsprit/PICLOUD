package com.transittn.organization_partner.dto;

import com.transittn.organization_partner.enums.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationDTO {
    private Long id;
    private String name;
    private String acronyme;
    private String email;
    private String phoneNumber;
    private String website;
    private String logo;
    private OrgType type;
    private OrgStatus status;
    private CoverageType coverageType;
}
