package mg.working.diakonacalendar.service.planning;


import lombok.RequiredArgsConstructor;
import mg.working.diakonacalendar.dto.PeriodePlanningDto;
import mg.working.diakonacalendar.dto.PlanningRangeResultDto;
import mg.working.diakonacalendar.entity.*;
import mg.working.diakonacalendar.exception.BadRequestException;
import mg.working.diakonacalendar.exception.NotFoundException;
import mg.working.diakonacalendar.mapper.DtoMapper;
import mg.working.diakonacalendar.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PlanningServiceImpl implements PlanningService {

    private final GroupeRepo groupeRepo;
    private final PeriodeRepo periodeRepo;
    private final DimancheRepo dimancheRepo;
    private final AffectationRepo affectationRepo;
    private final DebutMoisRepo debutMoisRepo;

    @Override
    public PeriodePlanningDto genererPlanning(int annee, int mois, boolean overwrite) {

        validateAnneeMois(annee, mois);

        List<Groupe> groupes = groupeRepo.findByActifTrueOrderByCodeAsc();
        if (groupes.size() != 5)
            throw new BadRequestException("Le système exige exactement 5 groupes actifs");

        Optional<Periode> exist = periodeRepo.findByAnneeAndMois(annee, mois);
        if (exist.isPresent()) {
            if (!overwrite) return buildPlanningDto(exist.get());
            periodeRepo.delete(exist.get());
            periodeRepo.flush();
        }

        // 1️⃣ Création période
        Periode periode = periodeRepo.save(
                Periode.builder().annee(annee).mois(mois).build()
        );

        // 2️⃣ Générer dimanches
        List<Dimanche> dimanches = dimancheRepo.saveAll(generateSundays(periode));

        List<Affectation> toSave = new ArrayList<>();

        // 3️⃣ Charger historique global
        Map<UUID, LocalDate> lastWorkedDate = new HashMap<>();
        Map<UUID, ServiceSlot> lastService = new HashMap<>();

        for (Groupe g : groupes) {
            affectationRepo
                    .findTopByGroupeIdOrderByDimanche_DateDimancheDesc(g.getId())
                    .ifPresent(a -> {
                        lastWorkedDate.put(g.getId(), a.getDimanche().getDateDimanche());
                        lastService.put(g.getId(), a.getService());
                    });
        }

        int cursor = 0;

        for (Dimanche d : dimanches) {

            int maxServices = d.isLast() ? 1 : 2;
            int assigned = 0;

            while (assigned < maxServices) {

                Groupe g = groupes.get(cursor % groupes.size());

                LocalDate lastDate = lastWorkedDate.get(g.getId());

                boolean workedLastSunday =
                        lastDate != null &&
                                lastDate.plusWeeks(1).equals(d.getDateDimanche());

                if (!workedLastSunday) {

                    ServiceSlot service =
                            lastService.getOrDefault(g.getId(), ServiceSlot.SERVICE_2)
                                    .opposite(); // alternance automatique

                    toSave.add(affect(d, g, service));

                    lastWorkedDate.put(g.getId(), d.getDateDimanche());
                    lastService.put(g.getId(), service);

                    assigned++;
                }

                cursor++;
            }
        }

        affectationRepo.saveAll(toSave);

        return buildPlanningDto(periode);
    }


    @Override
    @Transactional(readOnly = true)
    public PeriodePlanningDto getPlanning(int annee, int mois) {
        validateAnneeMois(annee, mois);
        Periode p = periodeRepo.findByAnneeAndMois(annee, mois)
                .orElseThrow(() -> new NotFoundException("Planning introuvable pour " + annee + "-" + mois));
        return buildPlanningDto(p);
    }

    @Override
    public void deletePlanning(int annee, int mois) {
        validateAnneeMois(annee, mois);
        Periode p = periodeRepo.findByAnneeAndMois(annee, mois)
                .orElseThrow(() -> new NotFoundException("Planning introuvable"));
        periodeRepo.delete(p);
    }

    @Override
    public PlanningRangeResultDto genererPlanningRange(int anneeDebut, int moisDebut,
                                                       int anneeFin, int moisFin,
                                                       boolean overwrite) {

        validateAnneeMois(anneeDebut, moisDebut);
        validateAnneeMois(anneeFin, moisFin);

        YearMonth start = YearMonth.of(anneeDebut, moisDebut);
        YearMonth end = YearMonth.of(anneeFin, moisFin);

        if (end.isBefore(start)) {
            throw new BadRequestException("La période de fin ne doit pas être avant la période de début");
        }

        List<PeriodePlanningDto> results = new ArrayList<>();

        YearMonth cur = start;
        while (!cur.isAfter(end)) {
            results.add(genererPlanning(cur.getYear(), cur.getMonthValue(), overwrite));
            cur = cur.plusMonths(1);
        }

        return new PlanningRangeResultDto(results.size(), results);
    }

    // ----------------- Helpers -----------------

    private void validateAnneeMois(int annee, int mois) {
        if (annee < 2000 || annee > 2100) throw new BadRequestException("Année invalide");
        if (mois < 1 || mois > 12) throw new BadRequestException("Mois invalide");
    }

    private List<Dimanche> generateSundays(Periode periode) {
        YearMonth ym = YearMonth.of(periode.getAnnee(), periode.getMois());
        LocalDate start = ym.atDay(1).with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        LocalDate end = ym.atEndOfMonth();

        List<LocalDate> dates = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusWeeks(1)) {
            dates.add(d);
        }

        if (dates.isEmpty()) throw new IllegalStateException("Aucun dimanche trouvé (impossible) ?");

        LocalDate first = dates.get(0);
        LocalDate last = dates.get(dates.size() - 1);

        List<Dimanche> dimanches = new ArrayList<>();
        for (LocalDate date : dates) {
            dimanches.add(Dimanche.builder()
                    .periode(periode)
                    .dateDimanche(date)
                    .first(date.equals(first))
                    .last(date.equals(last))
                    .build());
        }
        return dimanches;
    }

    private Affectation affect(Dimanche dimanche, Groupe groupe, ServiceSlot service) {
        return Affectation.builder()
                .dimanche(dimanche)
                .groupe(groupe)
                .service(service)
                .build();
    }

    @Transactional(readOnly = true)
    protected PeriodePlanningDto buildPlanningDto(Periode periode) {
        List<Dimanche> dimanches = dimancheRepo.findByPeriodeIdOrderByDateDimancheAsc(periode.getId());
        List<UUID> ids = dimanches.stream().map(Dimanche::getId).toList();

        List<Affectation> affs = ids.isEmpty() ? List.of() : affectationRepo.findByDimancheIdIn(ids);
        // groupe lazy -> on force un accès pour éviter sérialisation foireuse
        affs.forEach(a -> {
            a.getGroupe().getCode();
            a.getDimanche().getDateDimanche();
        });

        Map<UUID, List<Affectation>> map = affs.stream()
                .collect(Collectors.groupingBy(a -> a.getDimanche().getId()));

        DebutMois debut = debutMoisRepo.findByPeriodeId(periode.getId()).orElse(null);

        return DtoMapper.toPlanningDto(periode, debut, dimanches, map);
    }
}
