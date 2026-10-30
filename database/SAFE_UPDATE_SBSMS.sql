-- SBSMS safe, additive database update. BACK UP `sbsms` before running.
-- This script does not drop existing tables or delete existing records.
USE sbsms;

CREATE TABLE IF NOT EXISTS resident_id_sequence (
  sequence_name VARCHAR(40) PRIMARY KEY,
  last_value BIGINT NOT NULL
) ENGINE=InnoDB;

-- Initialize the sequence from the largest current numeric Resident ID only if it is absent.
INSERT IGNORE INTO resident_id_sequence(sequence_name, last_value)
SELECT 'resident', COALESCE(MAX(CAST(id_number AS UNSIGNED)), 0)
FROM residents
WHERE id_number REGEXP '^[0-9]+$';

-- Add nullable appointment fields to the unified request table. Existing rows are preserved.
ALTER TABLE service_requests ADD COLUMN IF NOT EXISTS appointment_date DATE NULL;
ALTER TABLE service_requests ADD COLUMN IF NOT EXISTS appointment_time VARCHAR(50) NULL;

-- Keep the old appointments table during transition; do not drop it until data and code have been verified.
