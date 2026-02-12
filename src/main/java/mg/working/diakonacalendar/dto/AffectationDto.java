package mg.working.diakonacalendar.dto;


import mg.working.diakonacalendar.entity.ServiceSlot;

import java.util.UUID;

public record AffectationDto(
        UUID id,
        UUID groupeId,
        String groupeCode,
        String groupeLibelle,
        ServiceSlot service
) {}
