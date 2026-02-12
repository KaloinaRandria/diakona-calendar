package mg.working.diakonacalendar.repository;




import mg.working.diakonacalendar.entity.Periode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PeriodeRepo extends JpaRepository<Periode, UUID> {
    Optional<Periode> findByAnneeAndMois(int annee, int mois);
    boolean existsByAnneeAndMois(int annee, int mois);
}
