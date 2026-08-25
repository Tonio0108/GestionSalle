-- =============================================================
-- Evolution : les occupations ont une heure precise
-- Cle primaire etendue : (codeprof, codesal, date, heure)
-- =============================================================

ALTER TABLE occuper ADD COLUMN IF NOT EXISTS heure TIME NOT NULL DEFAULT '08:00';

ALTER TABLE occuper DROP CONSTRAINT IF EXISTS occuper_pkey;

ALTER TABLE occuper ADD CONSTRAINT occuper_pkey
    PRIMARY KEY (codeprof, codesal, date, heure);

DROP INDEX IF EXISTS ux_occuper_salle_date;

CREATE UNIQUE INDEX IF NOT EXISTS ux_occuper_salle_date_heure
    ON occuper (codesal, date, heure);
