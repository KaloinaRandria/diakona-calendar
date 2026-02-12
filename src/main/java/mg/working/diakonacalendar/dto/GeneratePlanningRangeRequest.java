package mg.working.diakonacalendar.dto;

public record GeneratePlanningRangeRequest(
        Integer anneeDebut,
        Integer moisDebut,
        Integer anneeFin,
        Integer moisFin,
        Boolean overwrite
) {}
