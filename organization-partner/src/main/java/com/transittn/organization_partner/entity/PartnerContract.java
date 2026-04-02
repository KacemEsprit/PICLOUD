package com.transittn.organization_partner.entity;

import com.transittn.organization_partner.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "partner_contract")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerContract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ContractType contractType;

    @Enumerated(EnumType.STRING)
    private ContractStatus status;

    private Date startDate;
    private Date endDate;
    private String description;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @ManyToOne
    @JoinColumn(name = "partner_id")
    private Partner partner;
}
