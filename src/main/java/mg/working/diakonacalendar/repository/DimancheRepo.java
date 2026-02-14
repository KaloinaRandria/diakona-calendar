package mg.working.diakonacalendar.repository;


import mg.working.diakonacalendar.entity.Dimanche;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DimancheRepo extends JpaRepository<Dimanche, UUID> {
    List<Dimanche> findByPeriodeIdOrderByDateDimancheAsc(UUID periodeId);
    Optional<Dimanche> findByPeriodeIdAndFirstTrue(UUID periodeId);
    List<Dimanche> findBetween(LocalDate start, LocalDate end);
}

