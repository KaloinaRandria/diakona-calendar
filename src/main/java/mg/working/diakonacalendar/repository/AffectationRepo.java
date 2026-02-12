package mg.working.diakonacalendar.repository;


import mg.working.diakonacalendar.entity.Affectation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AffectationRepo extends JpaRepository<Affectation, UUID> {
    List<Affectation> findByDimancheIdIn(List<UUID> dimancheIds);
    List<Affectation> findByDimancheId(UUID dimancheId);
}
