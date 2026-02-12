package mg.working.diakonacalendar.service.affectation;



import mg.working.diakonacalendar.dto.AffectationDto;
import mg.working.diakonacalendar.entity.ServiceSlot;

import java.util.UUID;

public interface AffectationService {
    AffectationDto assign(UUID dimancheId, UUID groupeId, ServiceSlot service);
    void remove(UUID affectationId);
}
