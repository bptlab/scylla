package de.hpi.bpt.scylla.plugin.rembrandtConnector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class rembrandtConnectorUtils {

    /**
     * Plugin package identifier name
     */
    public static final String PLUGIN_NAME = "Rembrandt Connector";

    public static String getBackendUrl(){
        return "http://localhost:3000/api";
    };


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
            e.printStackTrace();
        }
        return jsonResult;

    }

}