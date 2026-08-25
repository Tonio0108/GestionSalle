-- =============================================================
-- Projet 10 - Gestion des salles de classe
-- Contraintes d'integrite supplementaires (idempotent)
-- =============================================================

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'chk_prof_nom') THEN
        ALTER TABLE prof ADD CONSTRAINT chk_prof_nom
            CHECK (char_length(btrim(nom)) BETWEEN 2 AND 100);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'chk_prof_prenom') THEN
        ALTER TABLE prof ADD CONSTRAINT chk_prof_prenom
            CHECK (char_length(btrim(prenom)) BETWEEN 2 AND 100);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'chk_prof_grade') THEN
        ALTER TABLE prof ADD CONSTRAINT chk_prof_grade
            CHECK (grade IS NULL OR grade IN ('Vacataire', 'Assistant', 'Maître de conférences', 'Professeur'));
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'chk_salle_designation') THEN
        ALTER TABLE salle ADD CONSTRAINT chk_salle_designation
            CHECK (char_length(btrim(designation)) BETWEEN 2 AND 150);
    END IF;
END $$;

CREATE UNIQUE INDEX IF NOT EXISTS ux_prof_nom_prenom
    ON prof (lower(btrim(nom)), lower(btrim(prenom)));

CREATE UNIQUE INDEX IF NOT EXISTS ux_salle_designation
    ON salle (upper(btrim(designation)));

DROP INDEX IF EXISTS ux_occuper_salle_date;

CREATE UNIQUE INDEX IF NOT EXISTS ux_occuper_salle_date_heure
    ON occuper (codesal, date, heure);
