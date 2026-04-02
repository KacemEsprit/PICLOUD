package com.transittn.organization_partner.dto;

import com.transittn.organization_partner.enums.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerDTO {
    private Long id;
    private String name;
    private String industrySector;
    private String partnershipType;
    private String email;
    private String phoneNumber;
    private String website;
    private String logo;
    private PartnerStatus status;
}
