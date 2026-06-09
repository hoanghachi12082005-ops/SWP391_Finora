package com.storemanagement.service.system;

import com.storemanagement.service.common.GenericService;

import com.storemanagement.dao.system.NotificationDAO;
import com.storemanagement.model.Notification;

public class NotificationService extends GenericService<Notification> {
    public NotificationService() {
        super(new NotificationDAO());
    }
}
