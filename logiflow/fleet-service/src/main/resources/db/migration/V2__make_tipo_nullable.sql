-- Make legacy column "tipo" nullable to avoid NOT NULL violations during inserts
ALTER TABLE vehiculos
    ALTER COLUMN tipo DROP NOT NULL;
