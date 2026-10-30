package service;

import dao.ServiceRequestDAO;
import model.ServiceRequest;
import java.util.List;

/** Application-level service request operations. */
public class RequestService {
    private final ServiceRequestDAO dao = new ServiceRequestDAO();
    public int submit(String residentId, int serviceId, double fee, String purpose) {
        if (residentId == null || residentId.isBlank() || purpose == null || purpose.isBlank() || fee < 0) return -1;
        return dao.add(residentId, serviceId, fee, purpose.trim());
    }
    public List<ServiceRequest> getAll() { return dao.getAll(); }
    public List<ServiceRequest> getForResident(String residentId) { return dao.getByResident(residentId); }
    public boolean updateStatus(int requestId, String status) {
        if (requestId <= 0 || status == null || !List.of("Pending", "Approved", "Processing", "Completed", "Rejected", "Cancelled").contains(status)) return false;
        return dao.setStatus(requestId, status);
    }
}
