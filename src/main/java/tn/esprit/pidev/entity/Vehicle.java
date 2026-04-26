package tn.transit.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import tn.transit.backend.entity.enums.VehicleStatus;
import tn.transit.backend.entity.enums.VehicleType;
import java.time.LocalDate;

@Entity
@Table(name = "vehicles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String plateNumber;
    private String brand;
    private Integer capacity;
    private Double mileage;

    @Enumerated(EnumType.STRING)
    private VehicleType type;

    @Enumerated(EnumType.STRING)
    private VehicleStatus status;

    private LocalDate purchaseDate;
}