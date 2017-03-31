package de.hpi.bpt.scylla.model.configuration;

import java.util.HashMap;
import java.util.Map;

import de.hpi.bpt.scylla.exception.ScyllaValidationException;

/**
 * Container class to describe branching behaviour.
 * 
 * @author Tsun Yin Wong
 *
 */
public class BranchingBehavior {

    Map<Integer, Double> branchingProbabilities = new HashMap<Integer, Double>();

    /**
     * Constructor.
     * 
     * @param probabilities
     *            map of node identifiers to values
     * @throws ScyllaValidationException
     */
    public BranchingBehavior(Map<Integer, Double> probabilities) throws ScyllaValidationException {
        // * @param normalize
        // * if true, all values are normalized so that their sum equals 1
        // if (normalize) {
        // // normalize so that sum of values is 1
        // Double sum = 0d;
        // for (Double value : probabilities.values()) {
        // sum += value;
        // }
        // for (Integer nodeId : probabilities.keySet()) {
        // Double value = probabilities.get(nodeId);
        // this.branchingProbabilities.put(nodeId, value / sum);
        // }
        // }
        // else {
        // already normalized, ensure that values are between 0 and 1
        for (Double value : probabilities.values()) {
            if (value < 0 || value > 1) {
                throw new ScyllaValidationException(
                        "Already normalized branching probability is out of bounds: " + value);
            }
        }
        this.branchingProbabilities = probabilities;
        // }
    }

    public Map<Integer, Double> getBranchingProbabilities() {
        return branchingProbabilities;
    }
}
