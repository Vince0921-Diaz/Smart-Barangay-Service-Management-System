package service;

import dao.AppoinmentDAO;
import model.Appointment;
import java.util.List;

/**
 * Application-level appointment operations. Class name retained for
 * compatibility with existing project references.
 */
public class AppoinmentService {

    private final AppoinmentDAO dao = new AppoinmentDAO();

    public boolean book(String residentId, int serviceId, String purpose, String date, String time) {
        if (residentId == null || residentId.isBlank() || purpose == null || purpose.isBlank() || date == null || time == null) {
            return false;
        }
        return dao.add(residentId, serviceId, purpose.trim(), date, time);
    }

    public List<Appointment> getAll() {
        return dao.getAll();
    }

    public List<Appointment> getForResident(String residentId) {
        return dao.getByResident(residentId);
    }

    public boolean updateStatus(int appointmentId, String status) {
        if (appointmentId <= 0 || status == null || !List.of("Pending", "Approved", "Completed", "Cancelled").contains(status)) {
            return false;
        }
        return dao.setStatus(appointmentId, status);
    }
}
