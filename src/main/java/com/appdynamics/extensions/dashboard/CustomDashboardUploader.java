package com.appdynamics.extensions.dashboard;

import com.appdynamics.extensions.http.Response;
import com.appdynamics.extensions.http.SimpleHttpClient;
import com.appdynamics.extensions.http.SimpleHttpClientBuilder;
import com.appdynamics.extensions.http.UrlBuilder;
import com.appdynamics.extensions.xml.Xml;
import org.apache.commons.httpclient.Header;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * Created by abey.tom on 4/11/15.
 */
public class CustomDashboardUploader {
    public static final Logger logger = LoggerFactory.getLogger(CustomDashboardUploader.class);

    public void uploadDashboard(String dashboardName, Xml xml, Map<String, String> argsMap, boolean overwrite) {
        SimpleHttpClient client = new SimpleHttpClientBuilder(argsMap).connectionTimeout(2000).socketTimeout(2000).build();
        Response response = client.target().path("controller/auth?action=login").get();
        if (response.getStatus() == 200) {
            Header[] headers = response.getHeaders();
            StringBuilder cookies = new StringBuilder();
            String csrf = null;
            for (Header header : headers) {
                if (header.getName().equalsIgnoreCase("set-cookie")) {
                    String value = header.getValue();
                    cookies.append(value).append(";");
                    if (value.contains("X-CSRF-TOKEN")) {
                        csrf = value.split("=")[1];
                    }

                }
            }

            boolean isPresent = isDashboardPresent(client, cookies, dashboardName);
            if (isPresent) {
                if (overwrite) {
                    uploadFile(dashboardName, xml, argsMap, cookies, csrf);
                } else {
                    logger.debug("The dashboard {} exists or API has been changed, not processing dashboard upload", dashboardName);
                }
            } else {
                uploadFile(dashboardName, xml, argsMap, cookies, csrf);
            }
        } else {
            logger.info("Custom Dashboard Upload Failed. The login to the controller is unsuccessful");
        }
    }

    private boolean isDashboardPresent(SimpleHttpClient client, StringBuilder cookies, String dashboardName) {
        Response response = client.target().path("controller/restui/dashboards/list/false")
                .header("Cookie", cookies.toString()).get();
        if (response.getStatus() == 200) {
            ArrayNode arrayNode = response.json(ArrayNode.class);
            boolean isPresent = false;
            if (arrayNode != null) {
                for (JsonNode jsonNode : arrayNode) {
                    String name = getTextValue(jsonNode.get("name"));
                    if (dashboardName.equals(name)) {
                        isPresent = true;
                    }
                }
            }
            return isPresent;
        } else {
            logger.error("The controller API [isDashboardPresent] returned invalid response{}, so cannot upload the dashboard"
                    ,response.getStatus());
            logger.info("Please change the [uploadDashboard] property in the config.yml to false. " +
                    "The xml will be written to the logs folder. Please import it to controller manually");
            return false;
        }
    }

    private void uploadFile(String instanceName, Xml xml, Map<String, String> argsMap, StringBuilder cookies, String csrf) {
        try {
            uploadFile(instanceName, xml, cookies, argsMap, csrf);
        } catch (IOException e) {
            logger.error("", e);
        }
    }

    public void uploadFile(String dashboardName, Xml xml, StringBuilder cookies, Map<String, String> argsMap, String csrf) throws IOException {
        String fileName = dashboardName + ".xml";
        String twoHyphens = "--";
        String boundary = "*****";
        String lineEnd = "\r\n";

        String urlStr = new UrlBuilder(argsMap).path("controller/CustomDashboardImportExportServlet").build();
        logger.info("Uploading the custom Dashboard {} to {}", dashboardName, urlStr);

        HttpURLConnection connection = null;
        URL url = new URL(urlStr);
        connection = (HttpURLConnection) url.openConnection();
        connection.setUseCaches(false);
        connection.setDoOutput(true);

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.setRequestProperty("Cache-Control", "no-cache");
        connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
        connection.setRequestProperty("Cookie", cookies.toString());
        DataOutputStream request = new DataOutputStream(connection.getOutputStream());

        request.writeBytes(twoHyphens + boundary + lineEnd);
        request.writeBytes("Content-Disposition: form-data; name=\"" + dashboardName + "\";filename=\"" + fileName + "\"" + lineEnd);
        request.writeBytes("Content-Type: text/xml" + lineEnd);
        request.writeBytes(lineEnd);
        request.write(xml.toString().getBytes());
        request.writeBytes(lineEnd + lineEnd);

        request.writeBytes(twoHyphens + boundary + lineEnd);
        request.writeBytes("Content-Disposition: form-data; name=\"X-CSRF-TOKEN\"" + lineEnd);
        request.writeBytes(lineEnd);
        request.writeBytes(csrf);
        request.writeBytes(lineEnd);

        request.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
        request.flush();
        request.close();
        InputStream inputStream = connection.getInputStream();
        int status = connection.getResponseCode();
        if (status == 200) {
            logger.info("Successfully Imported the dashboard {}", fileName);
        }
        inputStream.close();
    }

    private String getTextValue(JsonNode node) {
        if (node != null) {
            return node.getTextValue();
        }
        return null;
    }
}
