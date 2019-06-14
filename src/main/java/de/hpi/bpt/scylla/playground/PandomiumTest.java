package de.hpi.bpt.scylla.playground;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.browser.CefMessageRouter;
import org.cef.callback.CefQueryCallback;
import org.cef.handler.CefLoadHandlerAdapter;
import org.cef.handler.CefMessageRouterHandler;
import org.cef.handler.CefMessageRouterHandlerAdapter;
import org.panda_lang.pandomium.Pandomium;
import org.panda_lang.pandomium.settings.PandomiumSettings;
import org.panda_lang.pandomium.wrapper.PandomiumBrowser;
import org.panda_lang.pandomium.wrapper.PandomiumClient;


public class PandomiumTest {
	
	private Component component;
	private CefBrowser browser;
	
	public static void main(String[] args) {
		

        JFrame frame = new JFrame();
        frame.getContentPane().add(new PandomiumTest().component, BorderLayout.CENTER);

        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
            	CefApp.getInstance().dispose();
                frame.dispose();
            }
        });

        frame.setTitle("Pandomium");
        frame.setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
        frame.setVisible(true);
       
	}
	
	public PandomiumTest() {
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
                //browser.executeJavaScript("alert('ExecuteJavaScript works!');",browser.getURL(), 0);
        	}
		});
        PandomiumBrowser pandaBrowser = client.loadURL(url);
        browser = pandaBrowser.getCefBrowser();
        
        component = pandaBrowser.toAWTComponent();
		
	}
	
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
		params.add(callback);
		scanner.forEachRemaining(params::add);
		scanner.close();
		try {
			Method methodToCall = Arrays.stream(getClass().getMethods())
				.filter(each -> Objects.nonNull(each.getAnnotation(ExposeToJS.class)))
				.filter(each -> each.getName().equals(functionName))
				.findAny().orElseThrow(NoSuchMethodException::new);
			methodToCall.invoke(this, params.toArray());
//			getClass().getMethod(functionName, CefQueryCallback.class).invoke(this, callback);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		//callback.success(request);
	}
	
	@ExposeToJS
	public void foo(CefQueryCallback callback, String a, String b, String c) {
		System.out.println("foo: "+String.join(" ", new String[]{a, b, c}));
	}
	
	private void executeJavascript(String code) {
		browser.executeJavaScript(code, browser.getURL(), 0);
	}
}
