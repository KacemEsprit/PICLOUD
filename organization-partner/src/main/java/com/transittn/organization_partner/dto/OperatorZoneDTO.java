package com.transittn.organization_partner.dto;

import com.transittn.organization_partner.enums.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperatorZoneDTO {
    private Long id;
    private String governorate;
    private Region region;
    private Boolean isHeadquarter;
    private CoverageType coverageType;
    private Long organizationId;
}
