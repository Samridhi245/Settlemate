package strategies;

import exceptions.InvalidSplitException;

import java.util.List;
import java.util.Map;

public interface SplitStrategy {
    Map<String, Double> calculateShares(double amount, List<String> participants, Map<String, Double> inputDetails)
            throws InvalidSplitException;
}
