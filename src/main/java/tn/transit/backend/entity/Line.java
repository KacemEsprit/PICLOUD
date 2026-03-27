package tn.transit.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import tn.transit.backend.entity.enums.LineStatus;
import tn.transit.backend.entity.enums.TransportMode;
import java.util.List;

@Entity
@Table(name = "lines")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Line {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String code;

    @Enumerated(EnumType.STRING)
    private TransportMode mode;

    @Enumerated(EnumType.STRING)
    private LineStatus status;

    @OneToMany(mappedBy = "line",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<Stop> stops;

    @OneToMany(mappedBy = "line",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<Schedule> schedules;
}