package models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Split implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<String, Double> userShares;

    public Split() {
        this.userShares = new HashMap<>();
    }

    public Split(Map<String, Double> userShares) {
        this.userShares = new HashMap<>(userShares);
    }

    public Map<String, Double> getUserShares() {
        return userShares;
    }
}
