insert into groupe (id, code, libelle, actif) values
                                                  (gen_random_uuid(), 'FANILO', 'FANILO', true),
                                                  (gen_random_uuid(), 'FANASINA', 'FANASINA', true),
                                                  (gen_random_uuid(), 'FAHAZAVANA', 'FAHAZAVANA', true),
                                                  (gen_random_uuid(), 'FANANTENANA', 'FANANTENANA', true),
                                                  (gen_random_uuid(), 'FANDRESENA', 'FANDRESENA', true);

select affectation.service as service, g.code as groupe, p.mois as mois , p.annee as annee
from affectation
join public.groupe g on g.id = affectation.groupe_id
join public.dimanche d on d.id = affectation.dimanche_id
join public.periode p on p.id = d.periode_id