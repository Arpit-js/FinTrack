package com.fynance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BalanceService {

    private final ExpenseDAO expenseDAO = new ExpenseDAO();
    private final GroupMemberDAO groupMemberDAO = new GroupMemberDAO();
    private final SettlementDAO settlementDAO = new SettlementDAO();

    public Map<Integer, Double> calculateBalances(int groupId) {
        Map<Integer, Double> balances = new HashMap<>();

        // Initialize balances for all current group members
        List<GroupMember> members = groupMemberDAO.getMembersByGroup(groupId);
        for (GroupMember member : members) {
            balances.put(member.getUserId(), 0.0);
        }

        // 1. Calculate balances from expenses, assuming equal splits
        List<Expense> expenses = expenseDAO.getExpensesByGroup(groupId);
        for (Expense expense : expenses) {
            // Credit the payer
            balances.put(expense.getPaidByUserId(), balances.getOrDefault(expense.getPaidByUserId(), 0.0) + expense.getAmount());

            // Debit all members for their share
            if (!members.isEmpty()) {
                double share = expense.getAmount() / members.size();
                for (GroupMember member : members) {
                    balances.put(member.getUserId(), balances.getOrDefault(member.getUserId(), 0.0) - share);
                }
            }
        }

        // 2. Adjust balances with recorded settlements
        List<Settlement> settlements = settlementDAO.getSettlementsForGroup(groupId);
        for (Settlement settlement : settlements) {
            // Subtract from the payer and add to the receiver
            balances.put(settlement.getFromUserId(), balances.getOrDefault(settlement.getFromUserId(), 0.0) - settlement.getAmount());
            balances.put(settlement.getToUserId(), balances.getOrDefault(settlement.getToUserId(), 0.0) + settlement.getAmount());
        }

        return balances;
    }
}
