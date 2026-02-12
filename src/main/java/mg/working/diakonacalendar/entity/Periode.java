package mg.working.diakonacalendar.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Entity
@Table(name = "periode",
        uniqueConstraints = @UniqueConstraint(name = "uk_periode_annee_mois", columnNames = {"annee","mois"}))
public class Periode {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(nullable = false)
    private int annee;

    @Column(nullable = false)
    private int mois; // 1..12

    /**
     * Optionnel: utile si tu veux marquer rapidement qui est "leader" de la période
     * (mais on a aussi DebutMois pour service + alternance)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_groupe_id")
    private Groupe leaderGroupe;
}

