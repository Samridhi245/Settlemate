package strategies;

import exceptions.InvalidSplitException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PercentageSplitStrategy implements SplitStrategy {
    private static final double EPSILON = 0.01;

    @Override
    public Map<String, Double> calculateShares(double amount, List<String> participants, Map<String, Double> inputDetails)
            throws InvalidSplitException {
        if (participants == null || participants.isEmpty()) {
            throw new InvalidSplitException("Participants list cannot be empty for percentage split.");
        }
        if (inputDetails == null || inputDetails.isEmpty()) {
            throw new InvalidSplitException("Percentage split details cannot be empty.");
        }

        double percentageSum = 0.0;
        Map<String, Double> result = new HashMap<>();
        for (String userId : participants) {
            Double percentage = inputDetails.get(userId);
            if (percentage == null || percentage < 0) {
                throw new InvalidSplitException("Missing/invalid percentage for user: " + userId);
            }
            percentageSum += percentage;
            result.put(userId, amount * percentage / 100.0);
        }

        if (Math.abs(percentageSum - 100.0) > EPSILON) {
            throw new InvalidSplitException("Percentages must sum to 100.");
        }
        return result;
    }
}
