package script;

import dao.user.ProfileDao;
import model.Employee;

public class TestProfile {
    public static void main(String[] args) {
        ProfileDao dao = new ProfileDao();
        try {
            Employee emp = dao.getProfileById(1);
            if (emp != null) {
                System.out.println("Success: " + emp.getFullName());
            } else {
                System.out.println("Failed: Profile is null");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
