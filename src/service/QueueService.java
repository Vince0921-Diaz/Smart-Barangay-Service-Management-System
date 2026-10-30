package service;

import dao.AppoinmentDAO;
import model.Appointment;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/** Queue view derived from pending/approved appointments; no duplicate queue data is stored. */
public class QueueService {
    private final AppoinmentDAO appointmentDAO = new AppoinmentDAO();
    public List<Appointment> getQueue() {
        return appointmentDAO.getAll().stream()
                .filter(a -> "Pending".equalsIgnoreCase(a.getStatus()) || "Approved".equalsIgnoreCase(a.getStatus()))
                .collect(Collectors.toList());
    }
    public List<Appointment> searchQueue(String query) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        return getQueue().stream().filter(a -> q.isEmpty() || String.valueOf(a.getId()).contains(q)
                || a.getResidentName().toLowerCase(Locale.ROOT).contains(q)
                || a.getServiceName().toLowerCase(Locale.ROOT).contains(q)
                || a.getDate().toLowerCase(Locale.ROOT).contains(q)
                || a.getTime().toLowerCase(Locale.ROOT).contains(q)).collect(Collectors.toList());
    }
}
