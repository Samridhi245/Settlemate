package strategies;

import exceptions.InvalidSplitException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExactSplitStrategy implements SplitStrategy {
    private static final double EPSILON = 0.01;

    @Override
    public Map<String, Double> calculateShares(double amount, List<String> participants, Map<String, Double> inputDetails)
            throws InvalidSplitException {
        if (participants == null || participants.isEmpty()) {
            throw new InvalidSplitException("Participants list cannot be empty for exact split.");
        }
        if (inputDetails == null || inputDetails.isEmpty()) {
            throw new InvalidSplitException("Exact split details cannot be empty.");
        }

        double total = 0.0;
        Map<String, Double> result = new HashMap<>();
        for (String userId : participants) {
            Double share = inputDetails.get(userId);
            if (share == null || share < 0) {
                throw new InvalidSplitException("Missing/invalid exact share for user: " + userId);
            }
            result.put(userId, share);
            total += share;
        }

        if (Math.abs(total - amount) > EPSILON) {
            throw new InvalidSplitException("Exact shares must sum to total amount.");
        }
        return result;
    }
}
