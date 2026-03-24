package strategies;

import exceptions.InvalidSplitException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EqualSplitStrategy implements SplitStrategy {
    @Override
    public Map<String, Double> calculateShares(double amount, List<String> participants, Map<String, Double> inputDetails)
            throws InvalidSplitException {
        if (participants == null || participants.isEmpty()) {
            throw new InvalidSplitException("Participants list cannot be empty for equal split.");
        }

        double share = amount / participants.size();
        Map<String, Double> result = new HashMap<>();
        for (String userId : participants) {
            result.put(userId, share);
        }
        return result;
    }
}
