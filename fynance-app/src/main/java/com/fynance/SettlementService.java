package com.fynance;
// SettlementService.java (replace existing settleUp method with this)

import java.util.*;

public class SettlementService {

    public static class Transaction {

        String fromUser;
        String toUser;
        double amount;

        public Transaction(String fromUser, String toUser, double amount) {
            this.fromUser = fromUser;
            this.toUser = toUser;
            this.amount = amount;
        }

        @Override
        public String toString() {
            return String.format("%s pays %s ₹%.2f", fromUser, toUser, amount);
        }
    }

    /**
     * Greedy net-settlement: - Create list of debtors (negative balances) and
     * creditors (positive). - Sort debtors ascending (most negative first),
     * creditors descending (largest positive first). - Pair them greedily to
     * minimize number of transactions.
     */
    public List<Transaction> settleUp(Map<Integer, Double> balances, UserDAO userDAO) {
        List<Transaction> transactions = new ArrayList<>();
        List<Map.Entry<Integer, Double>> debtors = new ArrayList<>();
        List<Map.Entry<Integer, Double>> creditors = new ArrayList<>();

        for (var e : balances.entrySet()) {
            double val = Math.round(e.getValue() * 100.0) / 100.0; // round to 2 decimals
            if (val < -0.005) {
                debtors.add(Map.entry(e.getKey(), val));
            } else if (val > 0.005) {
                creditors.add(Map.entry(e.getKey(), val));
            }
        }

        debtors.sort(Comparator.comparingDouble(Map.Entry::getValue)); // most negative first
        creditors.sort((a, b) -> Double.compare(b.getValue(), a.getValue())); // most positive first

        int i = 0, j = 0;
        while (i < debtors.size() && j < creditors.size()) {
            Map.Entry<Integer, Double> d = debtors.get(i);
            Map.Entry<Integer, Double> c = creditors.get(j);
            double owe = -d.getValue();
            double toGet = c.getValue();
            double transfer = Math.min(owe, toGet);
            if (transfer > 0.009) {
                User fromUser = userDAO.getUserById(d.getKey());
                User toUser = userDAO.getUserById(c.getKey());
                String fromName = (fromUser != null) ? fromUser.getName() : "Unknown";
                String toName = (toUser != null) ? toUser.getName() : "Unknown";
                transactions.add(new Transaction(fromName, toName, Math.round(transfer * 100.0) / 100.0));
            }
            // update values
            double newDeb = d.getValue() + transfer; // less negative
            double newCred = c.getValue() - transfer;
            debtors.set(i, Map.entry(d.getKey(), newDeb));
            creditors.set(j, Map.entry(c.getKey(), newCred));

            // advance pointers if settled
            if (Math.abs(debtors.get(i).getValue()) < 0.01) {
                i++;
            }
            if (Math.abs(creditors.get(j).getValue()) < 0.01) {
                j++;
            }
        }
        return transactions;
    }
}
