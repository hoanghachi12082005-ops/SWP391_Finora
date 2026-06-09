package com.storemanagement.service.system;

import com.storemanagement.service.common.GenericService;

import com.storemanagement.dao.system.ActivityLogDAO;
import com.storemanagement.model.ActivityLog;

public class ActivityLogService extends GenericService<ActivityLog> {
    public ActivityLogService() {
        super(new ActivityLogDAO());
    }
}
