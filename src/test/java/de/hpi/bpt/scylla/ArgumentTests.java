package de.hpi.bpt.scylla;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.*;

public class ArgumentTests {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }
    
    @After
    public void cleanUpStreams() {
        System.setOut(null);
        System.setErr(null);
    }
    
    /**
     * There has to be a config file.
     */
    @Test
    public void missingConfigurationFile() {
        String[] args = {};
        Executable e = () -> Scylla.main(args);

        assertThrows(IllegalArgumentException.class, e);
    }

    @Test
    public void missingBpmnFile() {

    }
    
    @Test
    public void missingSimFile() {

    }

    @Test
    public void outputPath() {

    }

    @Test
    public void help() {
        String[] args = { "--help" };
        
        try {
			Scylla.main(args);
		} catch (Exception e) {
			throw new AssertionError("Help message does not working.");
        }
        
        assertTrue(outContent.toString().contains("Usage:"), "Help method.");
    }

}