DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.tables WHERE table_name = 'persons_simple'
  ) THEN
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_name = 'persons_simple' AND column_name = 'gender'
    ) THEN
      ALTER TABLE persons_simple ADD COLUMN gender VARCHAR(8);
    END IF;

    UPDATE persons_simple SET gender = 'MALE' WHERE gender IS NULL;

    ALTER TABLE persons_simple ALTER COLUMN gender SET NOT NULL;
  END IF;
END $$;
