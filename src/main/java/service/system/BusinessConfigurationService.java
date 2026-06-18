package service.system;

import service.common.GenericService;

import dao.system.BusinessConfigurationDAO;
import model.BusinessConfiguration;

public class BusinessConfigurationService extends GenericService<BusinessConfiguration> {
    public BusinessConfigurationService() {
        super(new BusinessConfigurationDAO());
    }
}
