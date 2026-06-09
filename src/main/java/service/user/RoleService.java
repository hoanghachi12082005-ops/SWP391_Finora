package com.storemanagement.service.user;

import com.storemanagement.service.common.GenericService;

import com.storemanagement.dao.user.RoleDAO;
import com.storemanagement.model.Role;

public class RoleService extends GenericService<Role> {
    public RoleService() {
        super(new RoleDAO());
    }
}
