package mg.working.diakonacalendar.dto;


import mg.working.diakonacalendar.entity.ServiceSlot;

import java.util.List;
import java.util.UUID;

public record PeriodePlanningDto(
        UUID periodeId,
        int annee,
        int mois,
        UUID leaderGroupeId,
        ServiceSlot leaderService,
        List<DimancheDto> dimanches
) {}
