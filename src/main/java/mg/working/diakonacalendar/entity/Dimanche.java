package mg.working.diakonacalendar.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Entity
@Table(name = "dimanche",
        uniqueConstraints = @UniqueConstraint(name = "uk_dimanche_periode_date", columnNames = {"periode_id","date_dimanche"}))
public class Dimanche {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "periode_id", nullable = false)
    private Periode periode;

    @Column(name = "date_dimanche", nullable = false)
    private LocalDate dateDimanche;

    @Column(name = "is_first", nullable = false)
    private boolean first;

    @Column(name = "is_last", nullable = false)
    private boolean last;
}

