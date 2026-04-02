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

    @NotNull(message = "Contract type is required")
    private ContractType contractType;

    private ContractStatus status;

    @NotNull(message = "Start date is required")
    private Date startDate;

    private Date endDate;
    private String description;

    @NotNull(message = "Organization is required")
    private Long organizationId;

    @NotNull(message = "Partner is required")
    private Long partnerId;
}