package de.hpi.bpt.scylla.playground;

import javax.swing.JFrame;

import org.junit.jupiter.api.Test;

public class PandomiumTests {
	
	@Test
	public void testRun() {
		JFrame frame = PandomiumTest.createFrame();
		try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
		frame.dispose();
	}

}
