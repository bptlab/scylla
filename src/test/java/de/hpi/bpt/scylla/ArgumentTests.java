package de.hpi.bpt.scylla;

import org.junit.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.*;

public class ArgumentTests {
    /**
     * There has to be a config file.
     */
    @Test
    public void missingConfigurationFile() {
        String[] args = {};
        Executable e = () -> Scylla.main(args);

        assertThrows(IllegalArgumentException.class, e);
    }
}