package com.transittn.organization_partner.dto;

import com.transittn.organization_partner.enums.*;
import lombok.*;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerContractDTO {
    private Long id;
    private ContractType contractType;
    private ContractStatus status;
    private Date startDate;
    private Date endDate;
    private String description;
    private Long organizationId;
    private Long partnerId;
}
