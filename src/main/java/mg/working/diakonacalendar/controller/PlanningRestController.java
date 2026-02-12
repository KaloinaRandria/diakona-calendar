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
     * /api/planning/2024/1  => planning de janvier 2024
       - Retourne le planning existant ou 404 si pas trouvé
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


    /**
     * /api/planning/generer-range
      {
        "anneeDebut": 2024,
        "moisDebut": 1,
        "anneeFin": 2024,
        "moisFin": 3,
        "overwrite": true
      }
       - Génère les plannings mensuels de janvier à mars 2024.
       - overwrite=true : regénère les plannings existants dans la plage
     **/
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
