package com.transittn.organization_partner.entity;

import com.transittn.organization_partner.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "partner")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Partner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String industrySector;
    private String partnershipType;
    private String email;
    private String phoneNumber;
    private String website;
    private String logo;

    @Enumerated(EnumType.STRING)
    private PartnerStatus status;

    @OneToMany(mappedBy = "partner", cascade = CascadeType.ALL)
    private List<PartnerContract> contracts;
}
