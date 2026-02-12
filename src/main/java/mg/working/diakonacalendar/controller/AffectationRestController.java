package mg.working.diakonacalendar.controller;


import lombok.RequiredArgsConstructor;
import mg.working.diakonacalendar.dto.AffectationDto;
import mg.working.diakonacalendar.dto.AssignGroupeRequest;
import mg.working.diakonacalendar.service.affectation.AffectationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/api/affectations")
@RequiredArgsConstructor
public class AffectationRestController {

    private final AffectationService affectationService;

    /**
     * Assigner un groupe à un dimanche et un service.
     * - Respecte les contraintes: unique (dimanche, service) et (dimanche, groupe)
     */
    @PostMapping("/dimanche/{dimancheId}")
    @ResponseStatus(HttpStatus.CREATED)
    public AffectationDto assign(@PathVariable UUID dimancheId, @RequestBody AssignGroupeRequest req) {
        return affectationService.assign(dimancheId, req.groupeId(), req.service());
    }

    @DeleteMapping("/{affectationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable UUID affectationId) {
        affectationService.remove(affectationId);
    }
}

