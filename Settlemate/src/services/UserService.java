package services;

import exceptions.UserNotFoundException;
import models.AppData;
import models.User;
import utils.IdGenerator;
import utils.InputValidator;

import java.util.ArrayList;
import java.util.List;

public class UserService {
    private final AppData appData;

    public UserService(AppData appData) {
        this.appData = appData;
    }

    public User createUser(String name, String email) {
        InputValidator.requireNonBlank(name, "Name");
        InputValidator.requireEmail(email);

        String userId = IdGenerator.generateId("USR");
        User user = new User(userId, name.trim(), email.trim());
        appData.getUsers().put(userId, user);
        return user;
    }

    public User getUserById(String userId) throws UserNotFoundException {
        User user = appData.getUsers().get(userId);
        if (user == null) {
            throw new UserNotFoundException("User not found: " + userId);
        }
        return user;
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(appData.getUsers().values());
    }
}
