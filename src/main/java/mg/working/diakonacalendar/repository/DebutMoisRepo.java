package mg.working.diakonacalendar.repository;



import mg.working.diakonacalendar.entity.DebutMois;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DebutMoisRepo extends JpaRepository<DebutMois, UUID> {
    Optional<DebutMois> findByPeriodeId(UUID periodeId);

    // Dernière fois où CE groupe a été leader (pour alterner service)
    Optional<DebutMois> findTopByGroupeIdOrderByPeriode_AnneeDescPeriode_MoisDesc(UUID groupeId);
}



