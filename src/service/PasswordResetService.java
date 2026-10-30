package service;

import database.DatabaseConnection;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import util.PasswordUtil;

/**
 * Password recovery operations. The generated OTP must be delivered through a configured
 * SMS provider; this class deliberately never displays or logs the OTP.
 */
public class PasswordResetService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final long OTP_LIFETIME_SECONDS = 300;

    public String createOtp(String accountId) throws SQLException {
        if (accountId == null || accountId.isBlank()) {
            throw new IllegalArgumentException("Account ID is required.");
        }
        String normalizedId = accountId.trim();
        String otp = String.format(java.util.Locale.ROOT, "%06d", RANDOM.nextInt(1_000_000));

        try (Connection c = DatabaseConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement check = c.prepareStatement(
                        "SELECT id_number FROM users WHERE id_number=?")) {
                    check.setString(1, normalizedId);
                    try (ResultSet rs = check.executeQuery()) {
                        if (!rs.next()) throw new IllegalArgumentException("Account could not be verified.");
                    }
                }
                try (PreparedStatement invalidate = c.prepareStatement(
                        "UPDATE password_reset_otps SET used=1 WHERE account_id=? AND used=0")) {
                    invalidate.setString(1, normalizedId);
                    invalidate.executeUpdate();
                }
                try (PreparedStatement insert = c.prepareStatement(
                        "INSERT INTO password_reset_otps(account_id, otp_hash, expires_at, used, created_at) VALUES(?,?,?,?,CURRENT_TIMESTAMP)")) {
                    insert.setString(1, normalizedId);
                    insert.setString(2, PasswordUtil.hash(otp));
                    insert.setTimestamp(3, Timestamp.from(Instant.now().plusSeconds(OTP_LIFETIME_SECONDS)));
                    insert.setBoolean(4, false);
                    insert.executeUpdate();
                }
                c.commit();
                return otp; // Caller must pass this to the SMS provider; never show it to the user.
            } catch (Exception ex) {
                c.rollback();
                if (ex instanceof SQLException) throw (SQLException) ex;
                if (ex instanceof IllegalArgumentException) throw (IllegalArgumentException) ex;
                throw new SQLException("Could not create password reset request.", ex);
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    public boolean verifyOtpAndReset(String accountId, String fullName, String contactNumber,
                                     String otp, String newPassword, String confirmPassword) throws SQLException {
        validatePassword(newPassword, confirmPassword);
        if (accountId == null || accountId.isBlank() || fullName == null || fullName.isBlank()
                || contactNumber == null || contactNumber.isBlank()
                || otp == null || !otp.matches("\\d{6}")) {
            return false;
        }

        String id = accountId.trim();
        try (Connection c = DatabaseConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                // Verify the account ID, full name, and registered resident contact number.
                // Contact details are read from the resident master record, not trusted from the form.
                String identitySql = "SELECT u.id_number FROM users u "
                        + "JOIN residents r ON r.id_number=u.id_number "
                        + "WHERE u.id_number=? AND LOWER(TRIM(u.full_name))=LOWER(TRIM(?)) "
                        + "AND REPLACE(REPLACE(REPLACE(TRIM(r.contact_number),' ',''),'-',''),'+','')="
                        + "REPLACE(REPLACE(REPLACE(TRIM(?),' ',''),'-',''),'+','') "
                        + "AND u.role='Resident'";
                try (PreparedStatement identity = c.prepareStatement(identitySql)) {
                    identity.setString(1, id);
                    identity.setString(2, fullName.trim());
                    identity.setString(3, contactNumber.trim());
                    try (ResultSet identityResult = identity.executeQuery()) {
                        if (!identityResult.next()) {
                            c.rollback();
                            return false;
                        }
                    }
                }

                long otpId;
                String storedHash;
                Timestamp expiresAt;
                boolean used;

                try (PreparedStatement p = c.prepareStatement(
                        "SELECT id, otp_hash, expires_at, used FROM password_reset_otps " +
                        "WHERE account_id=? ORDER BY id DESC LIMIT 1 FOR UPDATE")) {
                    p.setString(1, id);
                    try (ResultSet rs = p.executeQuery()) {
                        if (!rs.next()) {
                            c.rollback();
                            return false;
                        }
                        otpId = rs.getLong("id");
                        storedHash = rs.getString("otp_hash");
                        expiresAt = rs.getTimestamp("expires_at");
                        used = rs.getBoolean("used");
                    }
                }

                if (used || expiresAt == null || !expiresAt.toInstant().isAfter(Instant.now())
                        || !PasswordUtil.verify(otp, storedHash)) {
                    c.rollback();
                    return false;
                }

                try (PreparedStatement p = c.prepareStatement(
                        "UPDATE users SET password=? WHERE id_number=?")) {
                    p.setString(1, PasswordUtil.hash(newPassword));
                    p.setString(2, id);
                    if (p.executeUpdate() != 1) {
                        c.rollback();
                        return false;
                    }
                }

                try (PreparedStatement p = c.prepareStatement(
                        "UPDATE password_reset_otps SET used=1 WHERE account_id=? AND used=0")) {
                    p.setString(1, id);
                    p.executeUpdate();
                }
                c.commit();
                return true;
            } catch (SQLException | RuntimeException ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    public static void validatePassword(String password, String confirmation) {
        if (password == null || confirmation == null || !password.equals(confirmation)) {
            throw new IllegalArgumentException("Passwords do not match.");
        }
        if (password.length() < 8
                || !password.matches(".*[A-Za-z].*")
                || !password.matches(".*\\d.*")
                || !password.matches(".*[^A-Za-z0-9].*")) {
            throw new IllegalArgumentException(
                    "Password must be at least 8 characters and include a letter, a number, and a symbol.");
        }
    }
}
