package com.storemanagement.service.system;

import com.storemanagement.service.common.GenericService;

import com.storemanagement.dao.system.BusinessConfigurationDAO;
import com.storemanagement.model.BusinessConfiguration;

public class BusinessConfigurationService extends GenericService<BusinessConfiguration> {
    public BusinessConfigurationService() {
        super(new BusinessConfigurationDAO());
    }
}
