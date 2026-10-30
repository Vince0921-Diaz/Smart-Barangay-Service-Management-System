package service;

import dao.UserDAO;
import model.User;

/** Application-level authentication service. */
public class LoginService {
    private final UserDAO userDAO = new UserDAO();

    public User authenticate(String idNumber, String password) {
        if (idNumber == null || idNumber.isBlank() || password == null || password.isEmpty()) return null;
        return userDAO.authenticate(idNumber.trim(), password);
    }

    public boolean canAccessSystem(User user) {
        return user != null && "Approved".equalsIgnoreCase(user.getStatus());
    }
}
