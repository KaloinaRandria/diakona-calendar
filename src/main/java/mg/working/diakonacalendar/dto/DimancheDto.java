package mg.working.diakonacalendar.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record DimancheDto(
        UUID id,
        LocalDate dateDimanche,
        boolean first,
        boolean last,
        List<AffectationDto> affectations
) {}
