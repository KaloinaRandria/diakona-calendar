package mg.working.diakonacalendar.repository;

import mg.working.diakonacalendar.entity.Periode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanningRepo extends JpaRepository<Periode,Integer> {
}
