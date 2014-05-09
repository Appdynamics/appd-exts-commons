package com.appdynamics.extensions.http;

import com.appdynamics.extensions.io.Lines;
import com.appdynamics.extensions.io.WrapperInputStream;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.StatusLine;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created with IntelliJ IDEA.
 * User: abey.tom
 * Date: 4/8/14
 * Time: 2:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class Response {
    public static final Logger logger = LoggerFactory.getLogger(Response.class);

    private final int status;
    private final HttpMethodBase httpMethod;
    private final String url;
    private final SimpleHttpClient httpClient;

    public Response(int status, HttpMethodBase httpMethod, String url, SimpleHttpClient httpClient) {
        this.status = status;
        this.httpMethod = httpMethod;
        this.url = url;
        this.httpClient = httpClient;
    }

    public InputStream inputStream() throws IOException {
        InputStream in = httpMethod.getResponseBodyAsStream();
        if (in != null) {
            return new WrapperInputStream(in, httpMethod);
        }
        return in;
    }

    public String string() {
        try {
            String str = httpMethod.getResponseBodyAsString();
            return str;
        } catch (IOException e) {
            throw new RuntimeException("Exception while reading the input stream " + url, e);
        } finally {
            httpMethod.releaseConnection();
        }
    }

    public byte[] bytes() {
        try {
            return httpMethod.getResponseBody();
        } catch (IOException e) {
            logger.error("Exception while reading the response bytes " + url, e);
        } finally {
            httpMethod.releaseConnection();
        }
        return null;
    }

    public <T> T json(Class<T> clazz) {
        if (clazz != null) {
            InputStream in = null;
            try {
                in = inputStream();
                if (in != null) {
                    ObjectMapper mapper = new ObjectMapper();
                    return mapper.readValue(in, clazz);
                } else {
                    logger.warn("The Input stream from the url {} is null", url);
                    return null;
                }
            } catch (IOException e) {
                throw new RuntimeException("Error while building json object; url is " + url, e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("Type Not Set");
        }
    }

    public <T> T xml(Class<T> clazz) {
        if (clazz != null) {
            JAXBContext jaxbContext = httpClient.getJaxbContext();
            if (jaxbContext != null) {
                InputStream in = null;
                try {
                    in = inputStream();
                    if (in != null) {
                        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                        return (T) unmarshaller.unmarshal(in);
                    } else {
                        logger.warn("The Input stream from the url {} is null", url);
                        return null;
                    }
                } catch (JAXBException e) {
                    throw new RuntimeException("Exception while unmarshalling the data into XML." +
                            " Please make sure that the JAXB classes are added to the SimpleHttpClientBuilder", e);
                } catch (IOException e) {
                    throw new RuntimeException("Exception in fetching the data from " + url, e);
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                        }
                    }
                }
            } else {
                throw new RuntimeException("Cannot unmarshall because the JAXB Context cannot be instantiated.");
            }
        } else {
            throw new IllegalArgumentException("Type Not Set");
        }
    }

    public Header[] getHeaders() {
        return httpMethod.getResponseHeaders();
    }

    public String getHeader(String name) {
        Header header = httpMethod.getResponseHeader(name);
        if (header != null) {
            return header.getValue();
        }
        return null;
    }

    public long getContentLength() {
        return httpMethod.getResponseContentLength();
    }

    public String getStatusText() {
        return httpMethod.getStatusText();
    }

    public StatusLine getStatusLine() {
        return httpMethod.getStatusLine();
    }

    public void close() {
        try {
            InputStream in = httpMethod.getResponseBodyAsStream();
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
        }
        httpMethod.releaseConnection();
    }

    /**
     * Gets an Iterable which can be use to iterate over the response lines.
     *
     * @return a Lines which implements an iterator.
     */
    public Lines lines() {
        try {
            InputStream in = inputStream();
            if (in != null) {
                return new Lines(in, new CloseCallback() {
                    public void onClose() {
                        close();
                    }
                });
            } else {
                return null;
            }
        } catch (IOException e) {
            throw new RuntimeException("Exception while reading the input stream " + url, e);
        }
    }


    public int getStatus() {
        return status;
    }

    /**
     * This is a callback to listen to onClose.
     */
    public interface CloseCallback {
        public void onClose();
    }
}
