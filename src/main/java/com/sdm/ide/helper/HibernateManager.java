package com.sdm.ide.helper;

import com.sdm.ide.model.DatabaseModel;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class HibernateManager {

    private File hibernateFile;
    private Document doc;
    private static final String ELEMENT_ROOT = "session-factory";
    private static final String ELEMENT_MAPPING = "mapping";
    private static final String ELEMENT_PROP = "property";
    private static final String ATTR_CLASS = "class";
    private static final String ATTR_NAME = "name";

    private static HibernateManager instance;

    public synchronized static HibernateManager getInstance() {
        if (instance == null) {
            instance = new HibernateManager();
        }

        return instance;
    }

    public void load(File hibernateFile) throws ParserConfigurationException, SAXException, IOException {
        this.hibernateFile = hibernateFile;
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setValidating(false);
        docFactory.setNamespaceAware(true);
        docFactory.setFeature("http://xml.org/sax/features/namespaces", false);
        docFactory.setFeature("http://xml.org/sax/features/validation", false);
        docFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
        docFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        this.doc = docBuilder.parse(hibernateFile);
    }

    public File getFile() {
        return this.hibernateFile;
    }

    public DatabaseModel getDBInfo() throws URISyntaxException {
        NodeList properties = doc.getElementsByTagName(ELEMENT_PROP);
        DatabaseModel model = new DatabaseModel();
        for (int i = 0; i < properties.getLength(); i++) {
            Element property = (Element) properties.item(i);
            String name = property.getAttribute(ATTR_NAME);
            if (name.equalsIgnoreCase("hibernate.connection.username")) {
                model.setUser(property.getTextContent());
            } else if (name.equalsIgnoreCase("hibernate.connection.password")) {
                model.setPassword(property.getTextContent());
            } else if (name.equalsIgnoreCase("hibernate.default_schema")) {
                model.setSchema(property.getTextContent());
            } else if (name.equalsIgnoreCase("hibernate.connection.url")) {
                URI url = new URI(property.getTextContent().substring(5));
                String host = url.getHost();
                if (url.getPort() > 0) {
                    host += ":" + url.getPort();
                }
                model.setHost(host);
            }
        }
        return model;
    }

    public void setDBInfo(DatabaseModel model) {
        NodeList properties = doc.getElementsByTagName(ELEMENT_PROP);
        for (int i = 0; i < properties.getLength(); i++) {
            Element property = (Element) properties.item(i);
            String name = property.getAttribute(ATTR_NAME);
            if (name.equalsIgnoreCase("hibernate.connection.username")) {
                property.setTextContent(model.getUser());
            } else if (name.equalsIgnoreCase("hibernate.connection.password")) {
                property.setTextContent(model.getPassword());
            } else if (name.equalsIgnoreCase("hibernate.default_schema")) {
                property.setTextContent(model.getSchema());
            } else if (name.equalsIgnoreCase("hibernate.connection.url")) {
                property.setTextContent(model.getURL());
            }
        }
    }

    public void writeConfig() throws TransformerException {
        // write the content into xml file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(this.hibernateFile);
        transformer.transform(source, result);
    }

    public Set<String> getEntities() {
        NodeList mappings = doc.getElementsByTagName(ELEMENT_MAPPING);
        Set<String> entities = new HashSet<>();
        for (int i = 0; i < mappings.getLength(); i++) {
            Node mapping = mappings.item(i);
            NamedNodeMap attrs = mapping.getAttributes();
            Node entity = attrs.getNamedItem(ATTR_CLASS);
            entities.add(entity.getTextContent());
        }
        return entities;
    }

    public void clearMappings() {
        NodeList mappings = doc.getElementsByTagName(ELEMENT_MAPPING);
        for (int i = 0; i < mappings.getLength(); i++) {
            Node mapping = mappings.item(i);
            mapping.getParentNode().removeChild(mapping);
        }
    }

    public void removeMapping(String className) {
        NodeList mappings = doc.getElementsByTagName(ELEMENT_MAPPING);
        for (int i = 0; i < mappings.getLength(); i++) {
            Node mapping = mappings.item(i);
            String mapClass = mapping.getAttributes().getNamedItem(ATTR_CLASS).getTextContent();
            if (mapClass.equals(className)) {
                mapping.getParentNode().removeChild(mapping);
            }
        }

    }

    public void setEntites(Set<String> entities) {
        for (String entity : entities) {
            this.addEntity(entity);
        }
    }

    public void addEntity(String entityClass) {
        if (this.getEntities().contains(entityClass)) {
            return;
        }
        Node sessionFactory = doc.getElementsByTagName(ELEMENT_ROOT).item(0);
        Element mapping = doc.createElement(ELEMENT_MAPPING);
        mapping.setAttribute(ATTR_CLASS, entityClass);
        sessionFactory.appendChild(mapping);
    }
}
