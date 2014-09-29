package com.appdynamics.extensions.xml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * This is a simple wrapper around the java xml implementation to lookup the elements with simple xpath expressions.
 * <p/>
 * Created with IntelliJ IDEA.
 * User: abey.tom
 * Date: 4/30/14
 * Time: 2:33 PM
 */
public class Xml {
    public static final Logger logger = LoggerFactory.getLogger(Xml.class);
    private Node node;
    private Document document;

    public Xml(String xml) {
        try {
            document = getDocumentBuilder().parse(new InputSource(new StringReader(xml)));
        } catch (SAXException e) {
            logger.error("", e);
        } catch (IOException e) {
            logger.error("", e);
        }
    }

    public Xml(InputStream in) {
        try {
            document = getDocumentBuilder().parse(in);
        } catch (SAXException e) {
            logger.error("", e);
        } catch (IOException e) {
            logger.error("", e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {

                }
            }
        }
    }

    private DocumentBuilder getDocumentBuilder() {
        DocumentBuilder documentBuilder = LocalXmlObjectFactory.getDocumentBuilder();
        if (documentBuilder != null) {
            return documentBuilder;
        } else {
            throw new XmlException("There was a problem in initializing the DocumentBuilder. Please see the log for details");
        }
    }

    private XPath getXpath() {
        XPath xPath = LocalXmlObjectFactory.getXPath();
        if (xPath != null) {
            return xPath;
        } else {
            throw new XmlException("XPath cannot be initialized. Please check the logs for details.");
        }
    }

    public Xml(Node node) {
        this.node = node;
    }

    public static Xml fromString(String xml) {
        return new Xml(xml);
    }

    public static Xml from(Node node) {
        return new Xml(node);
    }

    public String getText(String xpathStr) {
        if (xpathStr != null) {
            xpathStr = xpathStr.trim();
        }
        try {
            XPathExpression expression = getXpath().compile(xpathStr);
            return expression.evaluate(getSource());
        } catch (XPathExpressionException e) {
            throw new XmlException("The xpath expression " + xpathStr + " doesn't seem to be valid", e);
        }
    }

    private Object getSource() {
        if (document != null) {
            return document;
        } else if (node != null) {
            return node;
        }
        return null;
    }

    public NodeList getNode(String xpathStr) {
        try {
            XPathExpression expression = getXpath().compile(xpathStr);
            return (NodeList) expression.evaluate(getSource(), XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            throw new XmlException("The xpath expression " + xpathStr + " doesn't seem to be valid", e);
        }
    }


    public static class XmlException extends RuntimeException {
        public XmlException(String message) {
            super(message);
        }

        public XmlException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    @Override
    public String toString() {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            if (document != null) {
                transformer.transform(new DOMSource(document), new StreamResult(writer));
            } else if (node != null) {
                transformer.transform(new DOMSource(node), new StreamResult(writer));
            }
            return writer.getBuffer().toString();
        } catch (Exception e) {
            logger.error("Error while transforming the xml document " + getSource(), e);
            return null;
        }
    }
}
