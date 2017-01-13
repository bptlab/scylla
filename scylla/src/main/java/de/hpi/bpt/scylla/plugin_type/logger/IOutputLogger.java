package de.hpi.bpt.scylla.plugin_type.logger;

import java.io.IOException;

import de.hpi.bpt.scylla.simulation.SimulationModel;

interface IOutputLogger {

    // public boolean isActive();

    // public void setActive(boolean active);

    public void writeToLog(SimulationModel model, String outputPathWithoutExtension) throws IOException;

}
