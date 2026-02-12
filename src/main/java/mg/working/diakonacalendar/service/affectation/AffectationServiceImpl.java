package mg.working.diakonacalendar.service.affectation;


import lombok.RequiredArgsConstructor;
import mg.working.diakonacalendar.dto.AffectationDto;
import mg.working.diakonacalendar.entity.Affectation;
import mg.working.diakonacalendar.entity.Dimanche;
import mg.working.diakonacalendar.entity.Groupe;
import mg.working.diakonacalendar.entity.ServiceSlot;
import mg.working.diakonacalendar.exception.BadRequestException;
import mg.working.diakonacalendar.exception.NotFoundException;
import mg.working.diakonacalendar.mapper.DtoMapper;
import mg.working.diakonacalendar.repository.AffectationRepo;
import mg.working.diakonacalendar.repository.DimancheRepo;
import mg.working.diakonacalendar.repository.GroupeRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AffectationServiceImpl implements AffectationService {

    private final DimancheRepo dimancheRepo;
    private final GroupeRepo groupeRepo;
    private final AffectationRepo affectationRepo;

    @Override
    public AffectationDto assign(UUID dimancheId, UUID groupeId, ServiceSlot service) {
        Dimanche d = dimancheRepo.findById(dimancheId)
                .orElseThrow(() -> new NotFoundException("Dimanche introuvable"));
        Groupe g = groupeRepo.findById(groupeId)
                .orElseThrow(() -> new NotFoundException("Groupe introuvable"));

        // Règle: dernier dimanche => 1 seul service (SERVICE_1)
        if (d.isLast() && service != ServiceSlot.SERVICE_1) {
            throw new BadRequestException("Dernier dimanche: seulement SERVICE_1 autorisé");
        }

        // On laisse la DB gérer les uniques, mais on peut pré-check
        List<Affectation> existing = affectationRepo.findByDimancheId(dimancheId);
        if (existing.stream().anyMatch(a -> a.getService() == service)) {
            throw new BadRequestException("Ce service est déjà pris pour ce dimanche");
        }
        if (existing.stream().anyMatch(a -> a.getGroupe().getId().equals(groupeId))) {
            throw new BadRequestException("Ce groupe est déjà affecté à ce dimanche");
        }

        Affectation a = Affectation.builder()
                .dimanche(d)
                .groupe(g)
                .service(service)
                .build();

        a = affectationRepo.save(a);
        // force init
        a.getGroupe().getCode();

        return DtoMapper.toDto(a);
    }

    @Override
    public void remove(UUID affectationId) {
        if (!affectationRepo.existsById(affectationId)) throw new NotFoundException("Affectation introuvable");
        affectationRepo.deleteById(affectationId);
    }
}

