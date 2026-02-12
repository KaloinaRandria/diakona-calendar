package mg.working.diakonacalendar.service.groupe;


import lombok.RequiredArgsConstructor;
import mg.working.diakonacalendar.dto.CreateGroupeRequest;
import mg.working.diakonacalendar.dto.GroupeDto;
import mg.working.diakonacalendar.dto.UpdateGroupeRequest;
import mg.working.diakonacalendar.entity.Groupe;
import mg.working.diakonacalendar.exception.BadRequestException;
import mg.working.diakonacalendar.exception.NotFoundException;
import mg.working.diakonacalendar.mapper.DtoMapper;
import mg.working.diakonacalendar.repository.GroupeRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class GroupeServiceImpl implements GroupeService {

    private final GroupeRepo groupeRepo;

    @Override
    @Transactional(readOnly = true)
    public List<GroupeDto> list(boolean actifOnly) {
        return (actifOnly ? groupeRepo.findByActifTrueOrderByCodeAsc() : groupeRepo.findAllByOrderByCodeAsc())
                .stream().map(DtoMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public GroupeDto get(UUID id) {
        Groupe g = groupeRepo.findById(id).orElseThrow(() -> new NotFoundException("Groupe introuvable"));
        return DtoMapper.toDto(g);
    }

    @Override
    public GroupeDto create(CreateGroupeRequest req) {
        if (req == null || req.code() == null || req.code().isBlank()) {
            throw new BadRequestException("code est obligatoire");
        }
        if (req.libelle() == null || req.libelle().isBlank()) {
            throw new BadRequestException("libelle est obligatoire");
        }
        if (groupeRepo.existsByCodeIgnoreCase(req.code().trim())) {
            throw new BadRequestException("Code déjà utilisé");
        }

        Groupe g = Groupe.builder()
                .code(req.code().trim().toUpperCase())
                .libelle(req.libelle().trim())
                .actif(true)
                .build();

        return DtoMapper.toDto(groupeRepo.save(g));
    }

    @Override
    public GroupeDto update(UUID id, UpdateGroupeRequest req) {
        Groupe g = groupeRepo.findById(id).orElseThrow(() -> new NotFoundException("Groupe introuvable"));

        if (req.libelle() != null && !req.libelle().isBlank()) {
            g.setLibelle(req.libelle().trim());
        }
        if (req.actif() != null) {
            g.setActif(req.actif());
        }

        return DtoMapper.toDto(g);
    }

    @Override
    public void delete(UUID id) {
        if (!groupeRepo.existsById(id)) throw new NotFoundException("Groupe introuvable");
        groupeRepo.deleteById(id);
    }
}

