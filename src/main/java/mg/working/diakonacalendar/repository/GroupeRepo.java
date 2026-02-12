package mg.working.diakonacalendar.repository;

import mg.working.diakonacalendar.entity.Groupe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface GroupeRepo extends JpaRepository<Groupe, UUID> {
    boolean existsByCodeIgnoreCase(String code);
    Optional<Groupe> findByCodeIgnoreCase(String code);
    List<Groupe> findByActifTrueOrderByCodeAsc();
    List<Groupe> findAllByOrderByCodeAsc();
}
