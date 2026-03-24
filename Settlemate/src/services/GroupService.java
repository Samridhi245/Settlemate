package services;

import exceptions.UserNotFoundException;
import models.AppData;
import models.Group;
import models.User;
import utils.IdGenerator;
import utils.InputValidator;

import java.util.ArrayList;
import java.util.List;

public class GroupService {
    private final AppData appData;
    private final UserService userService;

    public GroupService(AppData appData, UserService userService) {
        this.appData = appData;
        this.userService = userService;
    }

    public Group createGroup(String groupName) {
        InputValidator.requireNonBlank(groupName, "Group name");
        String groupId = IdGenerator.generateId("GRP");
        Group group = new Group(groupId, groupName.trim());
        appData.getGroups().put(groupId, group);
        return group;
    }

    public Group getGroupById(String groupId) {
        Group group = appData.getGroups().get(groupId);
        if (group == null) {
            throw new IllegalArgumentException("Group not found: " + groupId);
        }
        return group;
    }

    public Group getGroupByName(String groupName) {
        InputValidator.requireNonBlank(groupName, "Group name");
        Group matched = null;
        for (Group group : appData.getGroups().values()) {
            if (group.getGroupName().equalsIgnoreCase(groupName.trim())) {
                if (matched != null) {
                    throw new IllegalArgumentException("Multiple groups found with name '" + groupName + "'. Please use Group ID.");
                }
                matched = group;
            }
        }
        if (matched == null) {
            throw new IllegalArgumentException("Group not found: " + groupName);
        }
        return matched;
    }

    public void addMember(String groupId, String userId) throws UserNotFoundException {
        Group group = getGroupById(groupId);
        User user = userService.getUserById(userId);
        if (!group.getMemberUserIds().contains(user.getUserId())) {
            group.getMemberUserIds().add(user.getUserId());
        }
    }

    public void removeMember(String groupId, String userId) {
        Group group = getGroupById(groupId);
        group.getMemberUserIds().remove(userId);
    }

    public List<Group> getAllGroups() {
        return new ArrayList<>(appData.getGroups().values());
    }
}
