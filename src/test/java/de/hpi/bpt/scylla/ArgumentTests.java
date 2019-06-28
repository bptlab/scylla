package de.hpi.bpt.scylla;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.*;

public class ArgumentTests {
	private final PrintStream standardOut = System.out;
	private final PrintStream standardErr = System.out;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();

    @BeforeEach
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }
    
    @AfterEach
    public void cleanUpStreams() {
        System.setOut(standardOut);
        System.setErr(standardErr);
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