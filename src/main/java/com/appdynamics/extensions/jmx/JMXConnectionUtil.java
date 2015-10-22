package com.appdynamics.extensions.jmx;


import com.google.common.base.Strings;
import org.apache.log4j.Logger;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class JMXConnectionUtil {
    public static final Logger logger = Logger.getLogger(JMXConnectionUtil.class);

    private JMXConnectionConfig config;
    private MBeanServerConnection connection;
    private JMXConnector connector;

    public JMXConnectionUtil(JMXConnectionConfig config){
        this.config = config;
    }


    /**
     * Connects to the remote JMX server
     * @return JMXConnector or null
     * @throws java.io.IOException
     */
    public JMXConnector connect() throws IOException {
        JMXServiceURL url = new JMXServiceURL(config.getJMXServiceURL());
        final Map<String, Object> env = new HashMap<String, Object>();
        if(!Strings.isNullOrEmpty(config.getUsername())){
            env.put(JMXConnector.CREDENTIALS,new String[]{config.getUsername(),config.getPassword()});
            connector = JMXConnectorFactory.connect(url, env);
        }
        else{
            connector = JMXConnectorFactory.connect(url);
        }
        if(connector != null){
            connection = connector.getMBeanServerConnection();
        }
        return connector;
    }


    /**
     * Gets all the mbeans from a given connection.
     * @return Set<ObjectInstance> or null instead
     */
    public Set<ObjectInstance> getAllMBeans()  {
        try {
            //fetching all the mbeans
            Set<ObjectInstance> allMbeans = connection.queryMBeans(null, null);
            return allMbeans;
        } catch (IOException e) {
            logger.error("Unable to fetch Mbeans." , e);
        }
        return null;
    }

    /**
     *
     * @param objectName
     * @return MBeanAttributeInfo[] or null
     */
    public MBeanAttributeInfo[] fetchAllAttributesForMbean(ObjectName objectName) {
        try {
            // Fetch all attributes.
            MBeanAttributeInfo[] attributes = connection.getMBeanInfo(objectName).getAttributes();
            return attributes;
        } catch (InstanceNotFoundException e) {
            logger.error("Unable to fetch Attributes For " + objectName,e);
        } catch (IntrospectionException e) {
            logger.error("Unable to fetch Attributes For " + objectName,e);
        } catch (ReflectionException e) {
            logger.error("Unable to fetch Attributes For " + objectName,e);
        } catch (IOException e) {
            logger.error("Unable to fetch Attributes For " + objectName,e);
        }
        return null;
    }



    public Object getMBeanAttribute(ObjectName objectName, String name) {
        try {
            return connection.getAttribute(objectName, name);
        } catch (MBeanException e) {
            logger.error("Unable to fetch Mbeans Info " + objectName + "attrName=" + name,e);
        } catch (AttributeNotFoundException e) {
            logger.error("Unable to fetch Mbeans Info " + objectName + "attrName=" + name,e);
        } catch (InstanceNotFoundException e) {
            logger.error("Unable to fetch Mbeans Info " + objectName + "attrName=" + name,e);
        } catch (ReflectionException e) {
            logger.error("Unable to fetch Mbeans Info " + objectName + "attrName=" + name,e);
        } catch (IOException e) {
            logger.error("Unable to fetch Mbeans Info " + objectName + "attrName=" + name,e);
        }
        return null;
    }





    public void close() throws IOException {
        if(connector != null){
            if(logger.isDebugEnabled()) {
                logger.debug("Closing the connection");
            }
            connector.close();
        }
    }

}
