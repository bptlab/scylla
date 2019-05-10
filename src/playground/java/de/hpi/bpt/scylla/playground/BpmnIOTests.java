package de.hpi.bpt.scylla.playground;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;

import javafx.concurrent.Worker;
import netscape.javascript.JSException;
import netscape.javascript.JSObject;

/**
 * Interesting pages:
 * https://www.teamdev.com/jxbrowser
 * https://bpmn.io/toolkit/bpmn-js/
 * https://o7planning.org/de/11151/anleitung-javafx-webview-und-webengine
 * 
 * Basic bpmnjs pages
 * https://bpmn.io/toolkit/bpmn-js/examples/
 * https://cdn.rawgit.com/bpmn-io/bpmn-js-examples/dfceecba/url-viewer/resources/pizza-collaboration.bpmn
 * https://github.com/bpmn-io/bpmn-js-examples/tree/master/pre-packaged
 * https://github.com/bpmn-io/bpmn-js-examples/blob/master/modeler/app/app.js
 * webpack ./app.js -o ./app.bundled.js --mode development
 * grunt build
 * @author Leon Bein
 *
 */
@SuppressWarnings("restriction")
public class BpmnIOTests extends Application{
	
	private Stage stage;
	private Pane pane;
	
	private WebEngine webEngine;
	private JSObject window;

   public static void main(String[] args) {
	   launch(args);
   }

	@Override
	public void start(Stage stage) throws Exception {
		this.stage = stage;
		pane = new VBox();
		
		Button reloadButton = new Button("Reload");
		reloadButton.setOnAction(e -> {
			pane.getChildren().remove(1);
			initializeWebView();
		});
		pane.getChildren().add(reloadButton);
		
		initializeWebView();
		
        Scene scene = new Scene(pane);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
	}
	
	private void initializeWebView() {
		WebView browser = new WebView();
		webEngine = browser.getEngine();
		webEngine.setJavaScriptEnabled(true);
         
		File page = new File("./src/playground/resources/index.html");
		URL url;
		try {
			url = page.toURI().toURL();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return;
		}
		assert page.exists();
		webEngine.load(url.toString());
		webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {if (newValue == Worker.State.SUCCEEDED) {postLoad();}});

		pane.getChildren().add(browser);
		VBox.setVgrow(browser, Priority.ALWAYS);
	}
	
	private void postLoad() {
		window = (JSObject) webEngine.executeScript("window");
		window.setMember("java", this);
		//loadDiagram();
	}
	
	public void loadDiagram(JSObject callback) {
        //webEngine.executeScript("openFromUrl('https://cdn.rawgit.com/bpmn-io/bpmn-js-examples/dfceecba/url-viewer/resources/pizza-collaboration.bpmn');");
        File file = selectFile();
        if(file == null)return;
        try {
        	callback.call("call", null, new String(Files.readAllBytes(file.toPath())));
		} catch (JSException | IOException e) {
			e.printStackTrace();
		}
        
	}
	
	private File selectFile() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(new File("./samples"));
		fileChooser.setTitle("Open Resource File");
		return fileChooser.showOpenDialog(stage);
	}
	
	public void log(String s) {
		System.out.println(s);
	}
   
}
