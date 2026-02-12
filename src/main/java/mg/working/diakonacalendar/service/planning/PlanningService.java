package mg.working.diakonacalendar.service.planning;


import mg.working.diakonacalendar.dto.PeriodePlanningDto;
import mg.working.diakonacalendar.dto.PlanningRangeResultDto;

public interface PlanningService {
    PeriodePlanningDto genererPlanning(int annee, int mois, boolean overwrite);
    PeriodePlanningDto getPlanning(int annee, int mois);
    void deletePlanning(int annee, int mois);

    PlanningRangeResultDto genererPlanningRange(int anneeDebut, int moisDebut,
                                                int anneeFin, int moisFin,
                                                boolean overwrite);
}
