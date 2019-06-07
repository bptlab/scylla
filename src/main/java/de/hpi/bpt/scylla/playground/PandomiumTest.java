package de.hpi.bpt.scylla.playground;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.MalformedURLException;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.panda_lang.pandomium.Pandomium;
import org.panda_lang.pandomium.settings.PandomiumSettings;
import org.panda_lang.pandomium.wrapper.PandomiumBrowser;
import org.panda_lang.pandomium.wrapper.PandomiumClient;

public class PandomiumTest {
	
	public static void main(String[] args) {
		File page = new File("./src/playground/resources/index.html");
		String url;
		try {
			url = page.toURI().toURL().toString();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return;
		}
		assert page.exists();
    
        System.out.println("starting "+url);
        PandomiumSettings settings = PandomiumSettings.getDefaultSettings();

        Pandomium pandomium = new Pandomium(settings);
        pandomium.initialize();

        PandomiumClient client = pandomium.createClient();
        Component browser = client.loadURL(url).toAWTComponent();

        JFrame frame = new JFrame();
        frame.setBackground(Color.red);
        frame.getContentPane().setBackground(Color.green);
        browser.setBackground(Color.blue);
        frame.getContentPane().add(browser, BorderLayout.CENTER);

        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                frame.dispose();
            }
        });

        frame.setTitle("Pandomium");
        frame.setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
        frame.setVisible(true);
        
        try {
			Thread.sleep(500);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
        
        System.out.println(frame.getContentPane().getSize());
        System.out.println(browser.getSize());
	}
}
