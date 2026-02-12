package mg.working.diakonacalendar.service.groupe;


import mg.working.diakonacalendar.dto.CreateGroupeRequest;
import mg.working.diakonacalendar.dto.GroupeDto;
import mg.working.diakonacalendar.dto.UpdateGroupeRequest;

import java.util.List;
import java.util.UUID;

public interface GroupeService {
    List<GroupeDto> list(boolean actifOnly);
    GroupeDto get(UUID id);
    GroupeDto create(CreateGroupeRequest req);
    GroupeDto update(UUID id, UpdateGroupeRequest req);
    void delete(UUID id);
}

