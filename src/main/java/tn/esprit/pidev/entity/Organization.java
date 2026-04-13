package tn.esprit.pidev.entity;
import tn.esprit.pidev.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "organization")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Organization {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String acronyme;
    private String transportType;
    private String email;
    private String phoneNumber;
    private String website;
    private String logo;
    private String region;

    @Enumerated(EnumType.STRING)
    private OrgType type;

    @Enumerated(EnumType.STRING)
    private OrgStatus status;

    @Enumerated(EnumType.STRING)
    private CoverageType coverageType;

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL)
    private List<OperatorZone> zones;

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL)
    private List<PartnerContract> contracts;
}
