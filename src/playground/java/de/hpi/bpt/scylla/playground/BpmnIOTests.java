package de.hpi.bpt.scylla.playground;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.FileChooser;

import javafx.concurrent.Worker;
import netscape.javascript.JSException;
import netscape.javascript.JSObject;

/**
 * Interesting pages:
 * https://www.teamdev.com/jxbrowser
 * https://bpmn.io/toolkit/bpmn-js/
 * https://o7planning.org/de/11151/anleitung-javafx-webview-und-webengine
 * @author Leon Bein
 *
 */
@SuppressWarnings("restriction")
public class BpmnIOTests extends Application{
	
	private Stage stage;
	private WebEngine webEngine;
	private JSObject window;

   public static void main(String[] args) {
	   launch(args);
   }

	@Override
	public void start(Stage stage) throws Exception {
		this.stage = stage;
		
		initializeWebView();

        stage.show();
	}
	
	private void initializeWebView() throws MalformedURLException {
		WebView browser = new WebView();
		webEngine = browser.getEngine();
		webEngine.setJavaScriptEnabled(true);
         
		File page = new File("./src/playground/resources/index.html");
		URL url= page.toURI().toURL();
		assert page.exists();
		webEngine.load(url.toString());
		webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {if (newValue == Worker.State.SUCCEEDED) {postLoad();}});

        Scene scene = new Scene(new StackPane(browser), 640, 480);
        stage.setScene(scene);
	}
	
	private void postLoad() {
		window = (JSObject) webEngine.executeScript("window");
		window.setMember("java", this);
		//loadDiagram();
	}
	
	public void loadDiagram() {
        //webEngine.executeScript("openFromUrl('https://cdn.rawgit.com/bpmn-io/bpmn-js-examples/dfceecba/url-viewer/resources/pizza-collaboration.bpmn');");
        File file = selectFile();
        try {
			window.call("openXML", new String(Files.readAllBytes(file.toPath())));
		} catch (JSException | IOException e) {
			e.printStackTrace();
		}
        
	}
	
	private File selectFile() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Resource File");
		return fileChooser.showOpenDialog(stage);
	}
   
}
