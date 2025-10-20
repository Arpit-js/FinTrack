package com.fynance;

public class Settlement {

    private int id;
    private int groupId;
    private int fromUserId;
    private int toUserId;
    private double amount;

    public Settlement(int groupId, int fromUserId, int toUserId, double amount) {
        this.groupId = groupId;
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.amount = amount;
    }

    // Getters
    public int getGroupId() {
        return groupId;
    }

    public int getFromUserId() {
        return fromUserId;
    }

    public int getToUserId() {
        return toUserId;
    }

    public double getAmount() {
        return amount;
    }
}
