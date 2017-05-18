package de.hpi.bpt.scylla.parser;

import de.hpi.bpt.scylla.SimulationManager;
import de.hpi.bpt.scylla.model.SimulationInput;

/**
 * Abstract class for parsers.
 * 
 * @author Tsun Yin Wong
 *
 * @param <T>
 *            extends {@link de.hpi.bpt.scylla.model.SimulationInput}
 */
public abstract class Parser<T extends SimulationInput> implements IDOMParser<T> {

    protected SimulationManager simulationEnvironment;

    Parser(SimulationManager simEnvironment) {
        this.simulationEnvironment = simEnvironment;
    }

}