-- =============================================================
-- Evolution annulee : les occupations n'ont plus d'heure precise
-- Cle primaire d'origine : (codeprof, codesal, date)
-- =============================================================

ALTER TABLE occuper DROP CONSTRAINT IF EXISTS occuper_pkey;

ALTER TABLE occuper ADD CONSTRAINT occuper_pkey
    PRIMARY KEY (codeprof, codesal, date);

DROP INDEX IF EXISTS ux_occuper_salle_date_heure;

CREATE UNIQUE INDEX IF NOT EXISTS ux_occuper_salle_date
    ON occuper (codesal, date);

ALTER TABLE occuper DROP COLUMN IF EXISTS heure;
