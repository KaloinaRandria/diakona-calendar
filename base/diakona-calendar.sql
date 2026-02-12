-- 1) Groupes
create table groupe (
                        id           uuid primary key default gen_random_uuid(),
                        code         varchar(20) not null unique,     -- ex: G1, G2...
                        libelle      varchar(100) not null,
                        actif        boolean not null default true,
                        created_at   timestamptz not null default now()
);

-- 2) Période = un mois
create table periode (
                         id           uuid primary key default gen_random_uuid(),
                         annee        int not null,
                         mois         int not null check (mois between 1 and 12),
                         unique (annee, mois)
);

-- 3) Dimanches d'une période
create table dimanche (
                          id           uuid primary key default gen_random_uuid(),
                          periode_id   uuid not null references periode(id) on delete cascade,
                          date_dimanche date not null,
                          is_last      boolean not null default false,
                          unique (periode_id, date_dimanche)
);

-- 4) Affectations (2 groupes par dimanche, sauf dernier = 1)
create table affectation (
                             id            uuid primary key default gen_random_uuid(),
                             dimanche_id   uuid not null references dimanche(id) on delete cascade,
                             groupe_id     uuid not null references groupe(id),
                             slot          smallint not null check (slot in (1,2)),  -- 1 ou 2
                             created_at    timestamptz not null default now(),
                             unique (dimanche_id, slot),            -- 1 seul groupe par slot
                             unique (dimanche_id, groupe_id)        -- pas le même groupe 2 fois le même dimanche
);

-- Index utiles
create index idx_dimanche_periode on dimanche(periode_id);
create index idx_affectation_dimanche on affectation(dimanche_id);
create index idx_affectation_groupe on affectation(groupe_id);
