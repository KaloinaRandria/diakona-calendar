package mg.working.diakonacalendar.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Entity
@Table(name = "affectation",
        uniqueConstraints = {
                @UniqueConstraint(name="uk_affectation_dimanche_service", columnNames = {"dimanche_id","service"}),
                @UniqueConstraint(name="uk_affectation_dimanche_groupe", columnNames = {"dimanche_id","groupe_id"})
        }
)
public class Affectation {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dimanche_id", nullable = false)
    private Dimanche dimanche;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "groupe_id", nullable = false)
    private Groupe groupe;

    /**
     * ServiceSlotConverter stocke 1/2 en base.
     */
    @Column(nullable = false)
    private ServiceSlot service;
}

