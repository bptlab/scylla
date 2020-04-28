package de.hpi.bpt.scylla.plugin.rembrandtConnector;

import org.javatuples.Pair;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class rembrandtConnectorUtils {

    /**
     * Plugin package identifier name
     */
    public static final String PLUGIN_NAME = "Rembrandt Connector";

    public static Map<String, String> resourceTaskMap = new HashMap<>();
    public static Map<String, Queue<Pair<String, String>>> eventsWaitingMap = new HashMap<>();

    public static String getBackendUrl(){
        return "http://localhost:3000/api";
    };

    //Todo change name of Pseudotype
    public static String getPseudoResourceTypeName() { return "Worker"; };


    public static String getResponse( String requestURL) {
        String jsonResult = "";
        try {
            // get execution result
            //get its time field in seconds

            //establish connection
            URL url = new URL(requestURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            // set as get request
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/vnd.api+json");
            //if Request not ok throw error
            if (con.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + con.getResponseCode());
            }
            // read response

            BufferedReader br = new BufferedReader(new InputStreamReader((con.getInputStream())));

            String output;
            while ((output = br.readLine()) != null) {
                jsonResult += output;
            }
            con.disconnect();
            return jsonResult;

        } catch (IOException e) {
            System.out.println("No Rembrandt Backend Found!");
            e.printStackTrace();
        }
        return jsonResult;

    }

}