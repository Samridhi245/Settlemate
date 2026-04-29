package models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Group implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String groupId;
    private String groupName;
    private final List<String> memberUserIds;
    private final List<String> expenseIds;

    public Group(String groupId, String groupName) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.memberUserIds = new ArrayList<>();
        this.expenseIds = new ArrayList<>();
    }

    public String getGroupId() {
        return groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<String> getMemberUserIds() {
        return memberUserIds;
    }

    public List<String> getExpenseIds() {
        return expenseIds;
    }

    @Override
    public String toString() {
<<<<<<< HEAD
        return "Group{" +
                "groupId='" + groupId + '\'' +
                ", groupName='" + groupName + '\'' +
                ", members=" + memberUserIds.size() +
                ", expenses=" + expenseIds.size() +
                '}';
=======
        return groupName;
>>>>>>> 4b7c522 (Initial commit)
    }
}
