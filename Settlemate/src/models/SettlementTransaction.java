package models;

import java.io.Serializable;

public class SettlementTransaction implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String fromUserId;
    private final String toUserId;
    private final double amount;

    public SettlementTransaction(String fromUserId, String toUserId, double amount) {
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.amount = amount;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public String getToUserId() {
        return toUserId;
    }

    public double getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return "SettlementTransaction{" +
                "fromUserId='" + fromUserId + '\'' +
                ", toUserId='" + toUserId + '\'' +
                ", amount=" + amount +
                '}';
    }
}
