package de.hpi.bpt.scylla.GUI.ModelerPane;

import java.awt.Component;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import javax.swing.JFileChooser;

import org.cef.CefApp;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.browser.CefMessageRouter;
import org.cef.callback.CefQueryCallback;
import org.cef.handler.CefLoadHandlerAdapter;
import org.cef.handler.CefMessageRouterHandlerAdapter;
import org.panda_lang.pandomium.Pandomium;
import org.panda_lang.pandomium.settings.PandomiumSettings;
import org.panda_lang.pandomium.wrapper.PandomiumBrowser;
import org.panda_lang.pandomium.wrapper.PandomiumClient;

import de.hpi.bpt.scylla.GUI.FormManager;
import de.hpi.bpt.scylla.playground.ExposeToJS;

public class Modeler {
	//TODO: See if https://github.com/bpmn-io/diagram-js-origin looks good
	
	private static final Pandomium pandomium;
	static {
        PandomiumSettings settings = PandomiumSettings.getDefaultSettings();
        pandomium = new Pandomium(settings);
        pandomium.initialize();
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
        	CefApp.getInstance().dispose();
        }));
	}
	
	private Component component;
	private JSBridge bridge;
	
	private Optional<FormManager> formManager = Optional.empty();

	
	public Modeler() {
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


        PandomiumClient client = pandomium.createClient();
        CefMessageRouter msgRouter = CefMessageRouter.create();
		msgRouter.addHandler(new CefMessageRouterHandlerAdapter() {
			
			@Override
			public boolean onQuery(CefBrowser browser, CefFrame frame, long query_id, String request, boolean persistent, CefQueryCallback callback) {
			    bridge.javascriptCall(callback, request);
				return true;
			}
			
			@Override
			public void onQueryCanceled(CefBrowser browser, CefFrame frame, long query_id) {
			    return;
			}
		}, true);
        client.getCefClient().addMessageRouter(msgRouter);
        
        
        client.getCefClient().addLoadHandler(new CefLoadHandlerAdapter() {
        	
        	@Override
        	public void onLoadEnd(CefBrowser browser, CefFrame frame, int httpStatusCode) {
        		synchronized(lock) {lock.notify();};
                //browser.executeJavaScript("alert('ExecuteJavaScript works!');",browser.getURL(), 0);
        	}
		});
        PandomiumBrowser pandaBrowser = client.loadURL(url);
        bridge = new JSBridge(this, pandaBrowser.getCefBrowser());
        
        component = pandaBrowser.toAWTComponent();

	} Object lock = new Object();
	
	@ExposeToJS
	public void loadDiagram(CefQueryCallback callback) {
		new Thread(()->{
	        //webEngine.executeScript("openFromUrl('https://cdn.rawgit.com/bpmn-io/bpmn-js-examples/dfceecba/url-viewer/resources/pizza-collaboration.bpmn');");
	        System.out.println("Selecting file");
			File file = selectFile();
	        if(file == null)return;
	        System.out.println("Found file");
	        try {
				callback.success(new String(Files.readAllBytes(file.toPath())));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();
	}
	
	
	
	private File selectFile() {
		JFileChooser fileChooser = new JFileChooser("./samples");
		fileChooser.setDialogTitle("Open Resource File");
		fileChooser.showOpenDialog(null);
		return fileChooser.getSelectedFile();
	}
	
	public Component getComponent() {
		return component;
	}
	
	public void close() {
    	CefApp.getInstance().dispose();
	}
	
	@ExposeToJS
	public void print(String s) {
		System.out.println(s);
	}
	
	@ExposeToJS
	@Deprecated
	public void print2(String s1, String s2) {
		print(s1);
		print(s2);
	}
	
	@ExposeToJS
	public void foo(CefQueryCallback callback, String a, String b, String c) {
		System.out.println("foo: "+String.join(" ", new String[]{a, b, c}));
	}

	public void createNew() {
		File emptyDiagram = new File("./src/playground/resources/emptyDiagram.bpmn");
		if(!emptyDiagram.exists())throw new RuntimeException("Could not create empty diagram: Template not found");
		open(emptyDiagram);
	}
	
	//I call js to call me back with their contents
	public void save(String path) throws IOException{
		validatePath(path);
		bridge.callJavascript("save", path);
	}
	
	@ExposeToJS
	public void save(String path, String content) {
		try(FileWriter writer = new FileWriter(path)){
			writer.write(content);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void open(File file) {

		new Thread(()->{
			System.out.println("wait");
			synchronized(lock) {
			try {
				lock.wait();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}}
			System.out.println("unlocked");
			try {
				String content = new String (Files.readAllBytes(file.toPath()), Charset.forName("UTF-8"));
				bridge.callJavascript("openXML", content);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();
	}

	public void clear() {
		bridge.callJavascript("clear");
	}

	public String getProcessId() {
		// TODO Auto-generated method stub
		return "Process Id not yet implemented";
	}
	
	private void validatePath(String path) {
		Paths.get(path);//Invalid paths are not accepted		
	}
	
	@ExposeToJS
	public void modelChanged() {
		setSaved(false);		
	}
	
	public FormManager getFormManager() {
		return formManager.orElse(null);
	}

	public void setFormManager(FormManager formManager) {
		this.formManager = Optional.of(formManager);
	}
	
	private void setSaved(boolean b) {
		formManager.ifPresent(fm -> fm.setSaved(b));
	}
}
