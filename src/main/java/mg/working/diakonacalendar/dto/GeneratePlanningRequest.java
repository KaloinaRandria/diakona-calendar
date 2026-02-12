package mg.working.diakonacalendar.dto;

public record GeneratePlanningRequest(
        Integer annee,
        Integer mois,
        Boolean overwrite
) {}
