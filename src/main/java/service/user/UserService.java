package service.user;

import service.common.GenericService;

import dao.user.UserDAO;
import model.User;

public class UserService extends GenericService<User> {
    public UserService() {
        super(new UserDAO());
    }
}
