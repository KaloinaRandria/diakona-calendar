package mg.working.diakonacalendar.mapper;


import mg.working.diakonacalendar.dto.AffectationDto;
import mg.working.diakonacalendar.dto.DimancheDto;
import mg.working.diakonacalendar.dto.GroupeDto;
import mg.working.diakonacalendar.dto.PeriodePlanningDto;
import mg.working.diakonacalendar.entity.*;

import java.util.*;
import java.util.stream.Collectors;

public class DtoMapper {

    public static GroupeDto toDto(Groupe g) {
        return new GroupeDto(g.getId(), g.getCode(), g.getLibelle(), g.isActif());
    }

    public static AffectationDto toDto(Affectation a) {
        Groupe g = a.getGroupe();
        return new AffectationDto(
                a.getId(),
                g.getId(),
                g.getCode(),
                g.getLibelle(),
                a.getService()
        );
    }

    public static DimancheDto toDto(Dimanche d, List<Affectation> affects) {
        List<AffectationDto> affectationDtos = affects.stream()
                .sorted(Comparator.comparing(a -> a.getService().name()))
                .map(DtoMapper::toDto)
                .toList();

        return new DimancheDto(
                d.getId(),
                d.getDateDimanche(),
                d.isFirst(),
                d.isLast(),
                affectationDtos
        );
    }

    public static PeriodePlanningDto toPlanningDto(
            Periode p,
            DebutMois debutMois,
            List<Dimanche> dimanches,
            Map<UUID, List<Affectation>> affectationsParDimanche
    ) {
        List<DimancheDto> dimancheDtos = dimanches.stream()
                .map(d -> toDto(d, affectationsParDimanche.getOrDefault(d.getId(), List.of())))
                .toList();

        return new PeriodePlanningDto(
                p.getId(),
                p.getAnnee(),
                p.getMois(),
                debutMois != null ? debutMois.getGroupe().getId() : null,
                debutMois != null ? debutMois.getService() : null,
                dimancheDtos
        );
    }
}
