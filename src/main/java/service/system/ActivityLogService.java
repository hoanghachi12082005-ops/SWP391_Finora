package service.system;

import dao.system.ActivityLogDAO;

public class ActivityLogService {
    private final ActivityLogDAO dao = new ActivityLogDAO();

    public void log(Integer empId, String action, String table, Integer recordId, String oldData, String newData) {
        dao.log(empId, action, table, recordId, oldData, newData);
    }
}
