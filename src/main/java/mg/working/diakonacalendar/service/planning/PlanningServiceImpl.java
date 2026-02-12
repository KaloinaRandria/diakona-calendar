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

        // Vérifie groupes
        List<Groupe> groupes = groupeRepo.findByActifTrueOrderByCodeAsc();
        if (groupes.size() < 2) throw new BadRequestException("Il faut au moins 2 groupes actifs");
        if (groupes.size() != 5) {
            // tu peux enlever si tu veux être strict
             throw new BadRequestException("Le système attend 5 groupes actifs");
        }

        Optional<Periode> exist = periodeRepo.findByAnneeAndMois(annee, mois);
        if (exist.isPresent()) {
            if (!overwrite) {
                // retourne le planning existant
                return buildPlanningDto(exist.get());
            }
            // overwrite => supprime (cascade si tes relations sont bien configurées)
            periodeRepo.delete(exist.get());
            periodeRepo.flush();
        }

        // 1) Créer période
        Periode periode = Periode.builder().annee(annee).mois(mois).build();
        periode = periodeRepo.save(periode);

        // 2) Générer dimanches
        List<Dimanche> dimanches = generateSundays(periode);
        dimanches = dimancheRepo.saveAll(dimanches);

        // 3) Calculer leader du 1er dimanche (rotation stable)
        // Rotation simple: index = (annee*12 + (mois-1)) % N
        int n = groupes.size();
        int monthSerial = annee * 12 + (mois - 1);
        int leaderIndex = Math.floorMod(monthSerial, n);
        Groupe leader = groupes.get(leaderIndex);
        periode.setLeaderGroupe(leader);

        // 4) Alternance du service pour le leader (début de mois)
        ServiceSlot leaderService = debutMoisRepo.findTopByGroupeIdOrderByPeriode_AnneeDescPeriode_MoisDesc(leader.getId())
                .map(DebutMois::getService)
                .map(ServiceSlot::opposite)
                .orElse(ServiceSlot.SERVICE_1);

        DebutMois debutMois = DebutMois.builder()
                .periode(periode)
                .groupe(leader)
                .service(leaderService)
                .build();
        debutMoisRepo.save(debutMois);

        // 5) Affectations
        Dimanche firstSunday = dimanches.stream().filter(Dimanche::isFirst)
                .findFirst().orElseThrow(() -> new IllegalStateException("1er dimanche non trouvé"));

        // 5.1 1er dimanche: leader dans leaderService + autre groupe dans l'autre service
        List<Affectation> toSave = new ArrayList<>();
        toSave.add(affect(firstSunday, leader, leaderService));

        // second groupe simple: le suivant dans la liste (différent du leader)
        Groupe second = groupes.get((leaderIndex + 1) % n);
        toSave.add(affect(firstSunday, second, leaderService.opposite()));

        // 5.2 Autres dimanches: round-robin (2 groupes, dernier=1)
        int cursor = (leaderIndex + 2) % n; // on continue après leader+second
        for (Dimanche d : dimanches) {
            if (d.isFirst()) continue;

            if (d.isLast()) {
                Groupe g = groupes.get(cursor);
                toSave.add(affect(d, g, ServiceSlot.SERVICE_1)); // dernier dimanche = service 1 uniquement
                cursor = (cursor + 1) % n;
            } else {
                Groupe g1 = groupes.get(cursor);
                Groupe g2 = groupes.get((cursor + 1) % n);

                toSave.add(affect(d, g1, ServiceSlot.SERVICE_1));
                toSave.add(affect(d, g2, ServiceSlot.SERVICE_2));

                cursor = (cursor + 2) % n;
            }
        }

        affectationRepo.saveAll(toSave);

        // retourne dto
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
