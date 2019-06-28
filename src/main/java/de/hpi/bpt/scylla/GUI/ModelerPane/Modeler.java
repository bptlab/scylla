package de.hpi.bpt.scylla.GUI.ModelerPane;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

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

import de.hpi.bpt.scylla.playground.ExposeToJS;

public class Modeler {
	
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
	private CefBrowser browser;
	
	public static void main(String[] args) {
		

        JFrame frame = new JFrame();
        frame.getContentPane().add(new Modeler().component, BorderLayout.CENTER);

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
       
	}
	
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
			    javascriptCall(callback, request);
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
        browser = pandaBrowser.getCefBrowser();
        
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
	
	private void javascriptCall(CefQueryCallback callback, String request) {
		Scanner scanner = new Scanner(request);
		scanner.useDelimiter(":");
		String functionName = scanner.next();
		scanner.skip(":");
		scanner.useDelimiter(",");
		ArrayList<Object> params = new ArrayList<>();
		scanner.forEachRemaining(params::add);
		scanner.close();
		try {
			Method methodToCall = Arrays.stream(getClass().getMethods())
				.filter(each -> Objects.nonNull(each.getAnnotation(ExposeToJS.class)))
				.filter(each -> each.getName().equals(functionName))
				.findAny().orElseThrow(NoSuchMethodException::new);
			if(methodToCall.getParameterTypes().length > 0 && methodToCall.getParameterTypes()[0].equals(CefQueryCallback.class))params.add(callback);
			methodToCall.invoke(this, params.toArray());
//			getClass().getMethod(functionName, CefQueryCallback.class).invoke(this, callback);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		//callback.success(request);
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
	public void foo(CefQueryCallback callback, String a, String b, String c) {
		System.out.println("foo: "+String.join(" ", new String[]{a, b, c}));
	}
	
	private void executeJavascript(String code) {
		System.out.println(code);
		browser.executeJavaScript(code, browser.getURL(), 0);
	}
	
	private void callJavascript(String methodName, String... arguments) {
		executeJavascript(Arrays.stream(arguments)
			.map(Modeler::escapeAsJavascriptString)
			.collect(Collectors.joining(", ", methodName+'(', ")")));
	}
	
	/**Escape quotes, backslashes and line breaks*/
	private static Pattern forbiddenJavaScriptStringCharacters = Pattern.compile("\"|'|\\\\|\n");
	private static String escapeAsJavascriptString(String originalString) {
		  return '\''
				  +forbiddenJavaScriptStringCharacters
					  .matcher(originalString)
					  .replaceAll("\\\\$0")
			  +'\'';
	}

	public void createNew() {
		File emptyDiagram = new File("./src/playground/resources/emptyDiagram.bpmn");
		if(!emptyDiagram.exists())throw new RuntimeException("Could not create empty diagram: Template not found");
		open(emptyDiagram);
	}
	
	public void save(String path) throws IOException{
		validatePath(path);
		callJavascript("save", path);
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
				callJavascript("openXML", content);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();
	}

	public void clear() {
		callJavascript("clear");
	}

	public String getProcessId() {
		// TODO Auto-generated method stub
		return "Process Id not yet implemented";
	}
	
	private void validatePath(String path) {
		Paths.get(path);//Invalid paths are not accepted		
	}
}
