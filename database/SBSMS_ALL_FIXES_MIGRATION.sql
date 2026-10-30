-- SBSMS non-destructive compatibility migration
-- Back up the sbsms database before applying. This script does not drop tables or delete records.
USE sbsms;

-- Ensure fields used by the profile, registration and recovery forms exist.
ALTER TABLE residents ADD COLUMN IF NOT EXISTS middle_name VARCHAR(100) DEFAULT '';
ALTER TABLE residents ADD COLUMN IF NOT EXISTS government_id_type VARCHAR(100) DEFAULT '';
ALTER TABLE residents ADD COLUMN IF NOT EXISTS contact_number VARCHAR(30) DEFAULT '';
ALTER TABLE residents ADD COLUMN IF NOT EXISTS disability VARCHAR(100) DEFAULT 'None';
ALTER TABLE residents ADD COLUMN IF NOT EXISTS original_id_confirmed BOOLEAN DEFAULT FALSE;
ALTER TABLE residents ADD COLUMN IF NOT EXISTS status VARCHAR(30) NOT NULL DEFAULT 'Pending';
ALTER TABLE users ADD COLUMN IF NOT EXISTS status VARCHAR(30) NOT NULL DEFAULT 'Pending';
ALTER TABLE service_requests ADD COLUMN IF NOT EXISTS purpose VARCHAR(255) DEFAULT '';
ALTER TABLE appointments ADD COLUMN IF NOT EXISTS request_id INT NULL;

-- Status policy implemented in Java: Pending and Rejected requests may be approved;
-- Cancelled and Completed requests remain closed. Gender reporting is presentation-only
-- and now reports Male/Female while retaining all source records.
