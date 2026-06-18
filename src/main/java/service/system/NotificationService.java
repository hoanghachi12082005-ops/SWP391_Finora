package service.system;

import service.common.GenericService;

import dao.system.NotificationDAO;
import model.Notification;

public class NotificationService extends GenericService<Notification> {
    public NotificationService() {
        super(new NotificationDAO());
    }
}
