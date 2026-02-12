package mg.working.diakonacalendar.controller;

import lombok.RequiredArgsConstructor;
import mg.working.diakonacalendar.dto.GeneratePlanningRangeRequest;
import mg.working.diakonacalendar.dto.GeneratePlanningRequest;
import mg.working.diakonacalendar.dto.PeriodePlanningDto;
import mg.working.diakonacalendar.dto.PlanningRangeResultDto;
import mg.working.diakonacalendar.service.planning.PlanningService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/planning")
@RequiredArgsConstructor
public class PlanningRestController {

    private final PlanningService planningService;

    /**
     * Génère le planning d'un mois.
     * overwrite=true : regénère si existe
     */
    @PostMapping("/generer")
    @ResponseStatus(HttpStatus.CREATED)
    public PeriodePlanningDto generer(@RequestBody GeneratePlanningRequest req) {
        int annee = req.annee();
        int mois = req.mois();
        boolean overwrite = req.overwrite() != null && req.overwrite();
        return planningService.genererPlanning(annee, mois, overwrite);
    }

    /**
     * Récupère le planning d'un mois (si existe).
     */
    @GetMapping("/{annee}/{mois}")
    public PeriodePlanningDto getPlanning(@PathVariable int annee, @PathVariable int mois) {
        return planningService.getPlanning(annee, mois);
    }

    /**
     * Optionnel: supprimer un planning mensuel.
     */
    @DeleteMapping("/{annee}/{mois}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePlanning(@PathVariable int annee, @PathVariable int mois) {
        planningService.deletePlanning(annee, mois);
    }

    @PostMapping("/generer-range")
    @ResponseStatus(HttpStatus.CREATED)
    public PlanningRangeResultDto genererRange(@RequestBody GeneratePlanningRangeRequest req) {
        boolean overwrite = req.overwrite() != null && req.overwrite();
        return planningService.genererPlanningRange(
                req.anneeDebut(), req.moisDebut(),
                req.anneeFin(), req.moisFin(),
                overwrite
        );
    }

}
