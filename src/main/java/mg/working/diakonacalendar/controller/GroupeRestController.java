package mg.working.diakonacalendar.controller;

import lombok.RequiredArgsConstructor;
import mg.working.diakonacalendar.dto.CreateGroupeRequest;
import mg.working.diakonacalendar.dto.GroupeDto;
import mg.working.diakonacalendar.dto.UpdateGroupeRequest;
import mg.working.diakonacalendar.service.groupe.GroupeService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/groupes")
@RequiredArgsConstructor
@CrossOrigin("http://localhost:4200")
public class GroupeRestController {

    private final GroupeService groupeService;

    @GetMapping
    public List<GroupeDto> list(@RequestParam(defaultValue = "true") boolean actifOnly) {
        return groupeService.list(actifOnly);
    }

    @GetMapping("/{id}")
    public GroupeDto get(@PathVariable UUID id) {
        return groupeService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GroupeDto create(@RequestBody CreateGroupeRequest req) {
        return groupeService.create(req);
    }

    @PutMapping("/{id}")
    public GroupeDto update(@PathVariable UUID id, @RequestBody UpdateGroupeRequest req) {
        return groupeService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        groupeService.delete(id);
    }
}

