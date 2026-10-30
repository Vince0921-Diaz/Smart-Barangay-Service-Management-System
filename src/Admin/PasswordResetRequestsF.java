package Admin;

import database.DatabaseConnection;
import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import service.PasswordResetRequestService;

/**
 * Admin review screen for resident password reset requests.
 */
public class PasswordResetRequestsF extends JFrame {

    private final DefaultTableModel model = new DefaultTableModel(new Object[]{"Request ID", "Resident ID", "Full Name", "Reason", "Status", "Requested At"}, 0) {
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private final PasswordResetRequestService service = new PasswordResetRequestService();

    public PasswordResetRequestsF() {
        super("Password Reset Requests");
        setSize(900, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JButton refresh = new JButton("Refresh"), approve = new JButton("Approve"), reject = new JButton("Reject");
        refresh.addActionListener(e -> load());
        approve.addActionListener(e -> review(true));
        reject.addActionListener(e -> review(false));
        JPanel bar = new JPanel();
        bar.add(refresh);
        bar.add(approve);
        bar.add(reject);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(bar, BorderLayout.SOUTH);
        load();
    }

    private void load() {
        model.setRowCount(0);
        String sql = "SELECT pr.id,pr.user_id,u.full_name,pr.reason,pr.status,pr.requested_at FROM password_reset_requests pr JOIN users u ON u.id_number=pr.user_id ORDER BY pr.requested_at DESC";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql); ResultSet r = p.executeQuery()) {
            while (r.next()) {
                model.addRow(new Object[]{r.getInt(1), r.getString(2), r.getString(3), r.getString(4), r.getString(5), r.getTimestamp(6)});
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Could not load requests. Apply the password_reset_requests.sql migration first.");
        }
    }

    private Object[] selected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a request first.");
            return null;
        }
        int m = table.convertRowIndexToModel(row);
        return new Object[]{model.getValueAt(m, 0), model.getValueAt(m, 1), model.getValueAt(m, 4)};
    }

    private void review(boolean approve) {
        Object[] s = selected();
        if (s == null) {
            return;
        }
        if (!"Pending".equalsIgnoreCase(String.valueOf(s[2]))) {
            JOptionPane.showMessageDialog(this, "Only pending requests can be reviewed.");
            return;
        }
        String note = JOptionPane.showInputDialog(this, approve ? "Verification note (optional):" : "Reason for rejection:");
        if (!approve && (note == null || note.trim().isEmpty())) {
            JOptionPane.showMessageDialog(this, "Enter a rejection reason.");
            return;
        }
        try {
            if (service.review((Integer) s[0], "ADM001", approve, note)) {
                load();
                JOptionPane.showMessageDialog(this, approve ? "Request approved. The resident can now use Check Status / Reset Password on the Forgot Password screen." : "Request rejected.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Could not update request.");
        }
    }
}
