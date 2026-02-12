package mg.working.diakonacalendar.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Entity
@Table(name = "debut_mois",
        uniqueConstraints = @UniqueConstraint(name="uk_debut_mois_periode", columnNames = {"periode_id"}))
public class DebutMois {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "periode_id", nullable = false)
    private Periode periode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "groupe_id", nullable = false)
    private Groupe groupe;

    @Column(nullable = false)
    private ServiceSlot service; // 1 ou 2 (alternance à chaque retour comme leader)
}

