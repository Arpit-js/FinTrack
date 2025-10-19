package com.fynance;

public class GroupMember {

    private int id;
    private int groupId;
    private int userId;

    public GroupMember() {
    }

    public GroupMember(int id, int groupId, int userId) {
        this.id = id;
        this.groupId = groupId;
        this.userId = userId;
    }

    public GroupMember(int groupId, int userId) {
        this.groupId = groupId;
        this.userId = userId;
    }

    public int getId() {
        return id;
    }

    public int getGroupId() {
        return groupId;
    }

    public int getUserId() {
        return userId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "GroupMember{"
                + "id=" + id
                + ", groupId=" + groupId
                + ", userId=" + userId
                + '}';
    }
}
