package ui.controllers;

import controllers.AppController;
import models.User;

public class AuthUiController {
    private final AppController appController;

    public AuthUiController(AppController appController) {
        this.appController = appController;
    }

    public User login(String name) {
        return appController.loginByName(name);
    }

    public User register(String name, String email) {
        return appController.registerUser(name, email);
    }
}
