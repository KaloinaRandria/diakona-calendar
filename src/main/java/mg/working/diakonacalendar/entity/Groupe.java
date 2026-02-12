package mg.working.diakonacalendar.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Entity
@Table(name = "groupe",
        uniqueConstraints = @UniqueConstraint(name = "uk_groupe_code", columnNames = "code"))
public class Groupe {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, length = 20)
    private String code; // "G1".."G5"

    @Column(nullable = false, length = 100)
    private String libelle;

    @Column(nullable = false)
    private boolean actif = true;
}

