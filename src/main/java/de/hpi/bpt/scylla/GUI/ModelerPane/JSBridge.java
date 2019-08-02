package de.hpi.bpt.scylla.GUI.ModelerPane;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.cef.browser.CefBrowser;
import org.cef.callback.CefQueryCallback;

import de.hpi.bpt.scylla.playground.ExposeToJS;

public class JSBridge {
	
	private Object javaPart;
	private CefBrowser jsPart;
	
	public JSBridge(Object fromJava, CefBrowser toJs) {
		javaPart = fromJava;
		jsPart = toJs;
	}
	
	
	public void javascriptCall(CefQueryCallback callback, String request) {
		System.out.println(request);
		String requestWithoutEscaped = request.replace("\\\\", "␁");
		Object[] params = requestWithoutEscaped.split("\\\\");//This means "one slash", but is a regex
		String functionName = (String) params[0];
		for(int i = 1; i < params.length; i++) {
			params[i] = deJSONify(((String)params[i]).replace("␁", "\\"));
		}
		try {
			Method methodToCall = Arrays.stream(javaPart.getClass().getMethods())
				.filter(each -> Objects.nonNull(each.getAnnotation(ExposeToJS.class)))
				.filter(each -> each.getName().equals(functionName))
				.findAny().orElseThrow(NoSuchMethodException::new);
			if(methodToCall.getParameterTypes().length > 0 && methodToCall.getParameterTypes()[0].equals(CefQueryCallback.class))
				params[0] = callback;
			else
				params = Arrays.copyOfRange(params, 1, params.length);
			methodToCall.invoke(javaPart, params);
//			getClass().getMethod(functionName, CefQueryCallback.class).invoke(this, callback);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			callback.failure(1, e.getMessage());
		}
		//callback.success(request);
	}
	
	private static Object deJSONify(String json) {
		if(json.startsWith("\"") && json.endsWith("\"")) {//Object is a String
			return json
				.substring(1, json.length()-1)
				.replace("\\n", "\n")
				.replace("\\r", "\r")
				.replace("\\f", "\f")
				.replace("\\b", "\b")
				.replace("\\t", "\t")
				.replace("\\\"", "\"")
				.replace("\\\'", "\'")
				.replace("\\\\", "\\");
		}
		return json;
	}
	
	

	
	public void callJavascript(String methodName, String... arguments) {
		executeJavascript(Arrays.stream(arguments)
			.map(JSBridge::escapeAsJavascriptString)
			.collect(Collectors.joining(", ", methodName+'(', ")")));
	}
	
	private void executeJavascript(String code) {
		System.out.println(code);
		jsPart.executeJavaScript(code, jsPart.getURL(), 0);
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

}
