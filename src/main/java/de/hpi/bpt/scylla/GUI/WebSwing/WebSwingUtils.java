package de.hpi.bpt.scylla.GUI.WebSwing;

import java.awt.AWTException;
import java.awt.Robot;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import de.hpi.bpt.scylla.GUI.fileDialog.FileWriter;
import netscape.javascript.JSObject;

@SuppressWarnings("restriction")
public final class WebSwingUtils {
	
	public static String SESSIONID;
	public static JSObject JSGLOBAL;
	
	private static FileWriter FILEWRITER;
	private static ScrollBot SCROLLBOT;
	
	
	static String overlay = getResource("/Webswing/overlay.html");
	//static String upload_script = getResource("/Webswing/overlay.js");
	static String upload_utils = getResource("/Webswing/utils.js");

	
	public static void init(String sessionId) {
		SESSIONID = sessionId;
		JSGLOBAL = JSObject.getWindow(null);
		initFileWriter();
	}
	
	private static void initFileWriter() {
		FILEWRITER = new FileWriter("");
		JSGLOBAL.setMember("fileWriter",FILEWRITER);
		
		//Initializes overlay style
		JSGLOBAL.setMember("overlayNode",overlay);
		//Initializes util methods
		JSGLOBAL.eval(upload_utils);
		try {
			SCROLLBOT = new ScrollBot();
		} catch (AWTException e) {
			e.printStackTrace();
		}
		JSGLOBAL.call("setUp", SCROLLBOT);
	}
	
	public static FileWriter getDefaultFileWriter() {
		return FILEWRITER;
	}
	
	public static String getResource(String name) {
		InputStream inputStream = WebSwingUtils.class.getResourceAsStream(name);
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length;
		try {
			while ((length = inputStream.read(buffer)) != -1) {
			    result.write(buffer, 0, length);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result.toString();
	}
	
	public static class ScrollBot{
		private Robot scrollBot;
		private ScrollBot() throws AWTException{
			scrollBot = new Robot();
		}
		public synchronized void notifyScroll(int i) {
			scrollBot.mouseWheel(i);
			//System.err.println("Scrolling: "+i+" "+scrollBot);
		}
	}

}
