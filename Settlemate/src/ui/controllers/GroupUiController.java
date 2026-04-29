package ui.controllers;

import controllers.AppController;
import models.Group;
import models.User;

import java.util.List;

public class GroupUiController {
    private final AppController appController;

    public GroupUiController(AppController appController) {
        this.appController = appController;
    }

    public List<User> getAllUsers() {
        return appController.getAllUsers();
    }

    public List<Group> getAllGroups() {
        return appController.getAllGroups();
    }

    public Group createGroup(String name, List<String> memberIds) {
        return appController.createGroup(name, memberIds);
    }

    public void deleteGroup(String groupId) {
        appController.deleteGroup(groupId);
    }

    public Group updateGroup(String groupId, String newName, List<String> memberIds) {
        return appController.updateGroup(groupId, newName, memberIds);
    }
}
