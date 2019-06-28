package de.hpi.bpt.scylla.playground;

import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;

import org.junit.jupiter.api.Test;

public class PandomiumTests {
	
	@Test
	public void testRun() {
		JFrame frame = PandomiumTest.createFrame();
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				frame.dispose();
			}
		}, 1000l);
	}

}
