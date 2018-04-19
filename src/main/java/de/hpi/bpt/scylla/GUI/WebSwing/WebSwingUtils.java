package de.hpi.bpt.scylla.GUI.WebSwing;

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
	}
	
	public static FileWriter getDefaultFileWriter() {
		return FILEWRITER;
	}
	
	public static String getResource(String name) {
		InputStream inputStream = WebSwingUtils.class.getResourceAsStream("/resources"+name);
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
		// StandardCharsets.UTF_8.name() > JDK 7
		return result.toString();
	}

}
