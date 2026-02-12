package mg.working.diakonacalendar.dto;

import java.util.List;

public record PlanningRangeResultDto(
        int totalMois,
        List<PeriodePlanningDto> plannings
) {}
