-- Remove legacy column "tipo" that is not used by the discriminator
ALTER TABLE vehiculos
    DROP COLUMN IF EXISTS tipo;
