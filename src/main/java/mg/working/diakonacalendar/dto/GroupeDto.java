package mg.working.diakonacalendar.dto;


import java.util.UUID;

public record GroupeDto(
        UUID id,
        String code,
        String libelle,
        boolean actif
) {}
