package gui;

import java.awt.*;
import javax.swing.*;
import service.PasswordResetRequestService;

/**
 * Resident-facing password recovery: submit request, check approval, and set
 * own password.
 */
public class ForgotPassword extends JFrame {

    private final JTextField id = new JTextField(22), firstName = new JTextField(22), middleName = new JTextField(22), lastName = new JTextField(22);
    private final JTextArea reason = new JTextArea(3, 22);
    private final PasswordResetRequestService service = new PasswordResetRequestService();

    public ForgotPassword() {
        super("SBSMS - Password Reset");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(560, 480);
        setLocationRelativeTo(null);
        setResizable(false);
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(7, 10, 7, 10);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        addRow(form, g, 0, "ID No.", id);
        addRow(form, g, 1, "First Name", firstName);
        addRow(form, g, 2, "Middle Name", middleName);
        addRow(form, g, 3, "Last Name", lastName);
        addRow(form, g, 4, "Reason (optional)", new JScrollPane(reason));
        JLabel title = new JLabel("Password Recovery", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        JButton submit = new JButton("Submit Reset Request");
        submit.addActionListener(e -> submit());
        JButton check = new JButton("Check Status / Reset Password");
        check.addActionListener(e -> checkStatus());
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> dispose());
        JPanel buttons = new JPanel();
        buttons.add(submit);
        buttons.add(check);
        buttons.add(cancel);
        add(title, BorderLayout.NORTH);
        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
    }

    private void addRow(JPanel p, GridBagConstraints g, int row, String label, Component field) {
        g.gridy = row;
        g.gridx = 0;
        g.weightx = 0;
        p.add(new JLabel(label + ":"), g);
        g.gridx = 1;
        g.weightx = 1;
        p.add(field, g);
    }

    private void submit() {
        try {
            boolean ok = service.submit(id.getText(), firstName.getText(), middleName.getText(), lastName.getText(), reason.getText());
            if (ok) {
                JOptionPane.showMessageDialog(this, "Your request is submitted. The admin must verify and approve it. Use Check Status / Reset Password to check later.");
            } else {
                JOptionPane.showMessageDialog(this, "We could not match an approved resident account. Check your ID and name fields, or contact the barangay office.", "Unable to Submit", JOptionPane.WARNING_MESSAGE);
            }
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Could not submit the request. Check the database connection.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void checkStatus() {
        try {
            String status = service.getLatestStatus(id.getText(), firstName.getText(), middleName.getText(), lastName.getText());
            if (status == null) {
                JOptionPane.showMessageDialog(this, "No password reset request was found. Submit a request first.");
                return;
            }
            if ("Pending".equalsIgnoreCase(status)) {
                JOptionPane.showMessageDialog(this, "Your request is still pending admin review.");
                return;
            }
            if ("Rejected".equalsIgnoreCase(status)) {
                JOptionPane.showMessageDialog(this, "Your latest request was rejected. Please contact the barangay office for assistance.");
                return;
            }
            if ("Completed".equalsIgnoreCase(status)) {
                JOptionPane.showMessageDialog(this, "This reset request has already been used. Submit a new request if you still need help.");
                return;
            }
            if ("Approved".equalsIgnoreCase(status)) {
                JPasswordField p1 = new JPasswordField(), p2 = new JPasswordField();
                Object[] fields = {"ID No.: " + id.getText().trim(), "New password:", p1, "Confirm password:", p2};
                if (JOptionPane.showConfirmDialog(this, fields, "Set Your New Password", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
                    return;
                }
                if (service.resetLatestApproved(id.getText(), firstName.getText(), middleName.getText(), lastName.getText(), new String(p1.getPassword()), new String(p2.getPassword()))) {
                    JOptionPane.showMessageDialog(this, "Password reset successfully. You can now log in with your new password.");
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "No unused approved request was found. Refresh the status and try again.");
                }
            }
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Could not check status. Check the database connection.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
