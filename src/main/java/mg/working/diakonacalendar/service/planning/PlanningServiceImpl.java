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

        return genererPlanningRange(
                annee, mois,
                annee, mois,
                overwrite
        ).plannings().get(0);
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

        YearMonth startYM = YearMonth.of(anneeDebut, moisDebut);
        YearMonth endYM = YearMonth.of(anneeFin, moisFin);

        if (endYM.isBefore(startYM)) {
            throw new BadRequestException("La période de fin ne doit pas être avant la période de début");
        }

        // 1️⃣ Supprimer si overwrite
        if (overwrite) {
            YearMonth cur = startYM;
            while (!cur.isAfter(endYM)) {
                periodeRepo.findByAnneeAndMois(cur.getYear(), cur.getMonthValue())
                        .ifPresent(periodeRepo::delete);
                cur = cur.plusMonths(1);
            }
        }

        // 2️⃣ Créer toutes les périodes + dimanches
        YearMonth cur = startYM;
        List<Periode> periodes = new ArrayList<>();

        while (!cur.isAfter(endYM)) {

            Periode periode = periodeRepo.save(
                    Periode.builder()
                            .annee(cur.getYear())
                            .mois(cur.getMonthValue())
                            .build()
            );

            dimancheRepo.saveAll(generateSundays(periode));
            periodes.add(periode);

            cur = cur.plusMonths(1);
        }

        // 3️⃣ Génération GLOBALE continue
        LocalDate startDate = startYM.atDay(1);
        LocalDate endDate = endYM.atEndOfMonth();

        generate(startDate, endDate);

        // 4️⃣ Retour DTO
        List<PeriodePlanningDto> dtos =
                periodes.stream()
                        .map(this::buildPlanningDto)
                        .toList();

        return new PlanningRangeResultDto(dtos.size(), dtos);
    }

    @Override
    public void generate(LocalDate start, LocalDate end) {

        List<Dimanche> dimanches = dimancheRepo.findByDateDimancheBetween(start, end);
        List<Groupe> groupes = groupeRepo.findAll();

        groupes.sort(Comparator.comparing(Groupe::getLibelle));

        Map<UUID, LocalDate> lastWork = new HashMap<>();
        Map<UUID, ServiceSlot> lastService = new HashMap<>();

        int index = 0; // rotation globale

        for (Dimanche d : dimanches) {

            int assigned = 0;
            int attempts = 0;

            while (assigned < 2 && attempts < groupes.size()) {

                Groupe g = groupes.get(index % groupes.size());
                index++;
                attempts++;

                // règle : pas deux dimanches de suite
                if (lastWork.containsKey(g.getId())
                        && lastWork.get(g.getId()).equals(d.getDateDimanche().minusWeeks(1))) {
                    continue;
                }

                // alternance service
                ServiceSlot service =
                        lastService.getOrDefault(g.getId(), ServiceSlot.SERVICE_2)
                                .opposite();

                affectationRepo.save(
                        new Affectation(UUID.randomUUID(), d, g, service)
                );

                lastWork.put(g.getId(), d.getDateDimanche());
                lastService.put(g.getId(), service);

                assigned++;
            }
        }
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
