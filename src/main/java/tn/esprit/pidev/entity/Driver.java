package tn.transit.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import tn.transit.backend.entity.enums.DriverStatus;
import tn.transit.backend.entity.enums.LicenseType;
import tn.transit.backend.entity.enums.LicenseValidationStatus;
import java.time.LocalDate;

@Entity
@Table(name = "drivers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;
    private String phone;
    private String licenseNumber;
    private String licenseImagePath;
    private String aiExtractedData;
    private String aiValidationNote;

    @Enumerated(EnumType.STRING)
    private LicenseType licenseType;

    private LocalDate licenseExpiryDate;
    private Integer experienceYears;

    @Enumerated(EnumType.STRING)
    private DriverStatus status;

    @Enumerated(EnumType.STRING)
    private LicenseValidationStatus validationStatus;

    private String rejectionReason;

    @ManyToOne
    @JoinColumn(name = "vehicle_id")
    private Vehicle assignedVehicle;
}