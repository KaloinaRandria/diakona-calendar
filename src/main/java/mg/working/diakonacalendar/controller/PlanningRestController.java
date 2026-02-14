package mg.working.diakonacalendar.controller;

import lombok.RequiredArgsConstructor;
import mg.working.diakonacalendar.dto.GeneratePlanningRangeRequest;
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
     * Génération UNIQUE recommandée :
     * Génère une période complète (même pour un seul mois)
     *
     * Garantit :
     * - Rotation continue
     * - Pas deux dimanches consécutifs
     * - Alternance service respectée
     */
    @PostMapping("/generate")
    @ResponseStatus(HttpStatus.CREATED)
    public PlanningRangeResultDto generate(@RequestBody GeneratePlanningRangeRequest req) {

        boolean overwrite = req.overwrite() != null && req.overwrite();

        return planningService.genererPlanningRange(
                req.anneeDebut(),
                req.moisDebut(),
                req.anneeFin(),
                req.moisFin(),
                overwrite
        );
    }

    /**
     * Récupérer un planning mensuel
     */
    @GetMapping("/{annee}/{mois}")
    public PeriodePlanningDto getPlanning(@PathVariable int annee,
                                          @PathVariable int mois) {
        return planningService.getPlanning(annee, mois);
    }

    /**
     * Supprimer un planning mensuel
     */
    @DeleteMapping("/{annee}/{mois}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePlanning(@PathVariable int annee,
                               @PathVariable int mois) {
        planningService.deletePlanning(annee, mois);
    }


}
