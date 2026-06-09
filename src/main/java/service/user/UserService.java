package com.storemanagement.service.user;

import com.storemanagement.service.common.GenericService;

import com.storemanagement.dao.user.UserDAO;
import com.storemanagement.model.User;

public class UserService extends GenericService<User> {
    public UserService() {
        super(new UserDAO());
    }
}
