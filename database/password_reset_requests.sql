-- BACK UP the sbsms database before running this script.
-- Creates the admin-assisted password recovery queue. Does not delete or alter existing accounts.
CREATE TABLE IF NOT EXISTS password_reset_requests (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id VARCHAR(50) NOT NULL,
  reason VARCHAR(500) NOT NULL DEFAULT '',
  status ENUM('Pending','Approved','Rejected','Completed') NOT NULL DEFAULT 'Pending',
  requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  reviewed_at TIMESTAMP NULL DEFAULT NULL,
  reviewed_by VARCHAR(50) NULL,
  review_note VARCHAR(500) NOT NULL DEFAULT '',
  completed_at TIMESTAMP NULL DEFAULT NULL,
  INDEX idx_reset_request_status (status),
  INDEX idx_reset_request_user (user_id),
  CONSTRAINT fk_reset_request_user FOREIGN KEY (user_id) REFERENCES users(id_number) ON DELETE CASCADE
) ENGINE=InnoDB;
