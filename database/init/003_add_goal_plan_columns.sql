DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.tables WHERE table_name = 'persons_simple'
  ) THEN
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_name = 'persons_simple' AND column_name = 'target_change_kg'
    ) THEN
      ALTER TABLE persons_simple ADD COLUMN target_change_kg DOUBLE PRECISION;
    END IF;

    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_name = 'persons_simple' AND column_name = 'target_duration_weeks'
    ) THEN
      ALTER TABLE persons_simple ADD COLUMN target_duration_weeks INTEGER;
    END IF;

    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_name = 'persons_simple' AND column_name = 'training_frequency_per_week'
    ) THEN
      ALTER TABLE persons_simple ADD COLUMN training_frequency_per_week INTEGER;
    END IF;
  END IF;
END $$;
