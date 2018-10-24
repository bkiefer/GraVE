package de.dfki.vsm.xtesting.NewPropertyManager.model;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.dfki.vsm.model.project.AgentConfig;
import de.dfki.vsm.model.project.PluginConfig;
import de.dfki.vsm.runtime.project.RunTimeProject;

/**
 * Created by alvaro on 6/4/16.
 */
public class ProjectConfigWrapper {

  RunTimeProject project;

  public ProjectConfigWrapper(RunTimeProject pProject) {
    project = pProject;
  }

  public void addNewAgent(String agent, String pluginName) {

    String newXMLAgent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<Project name=\"" + project.getProjectName() + "\">"
            + "<Agents>"
            + "    <Agent name=\"" + agent + "\" device=\"" + pluginName + "\">"
            + "    </Agent>"
            + " </Agents>"
            + "</Project>";
    project.parseProjectConfigFromString(newXMLAgent);
    saveConfig();
  }

  public void saveConfig() {
    //TODO: Remove later
    File f = new File(project.getProjectPath());
    project.write(f);
  }

  public void changePluginName(PluginConfig pluginConfig, String oldPluginName, String newPluginName) throws Exception {
    String pluginXML = "<Project name=\"" + project.getProjectName() + "\">"
            + "<Plugins>"
            + pluginConfig.toString()
            + "</Plugins>"
            + "</Project>";
    String newXMLPlugin = changeXMLAttributeNode(pluginXML, "Plugin", "name", oldPluginName, newPluginName);
    if (newXMLPlugin != null) {
      removeOldPlugin(oldPluginName);
      project.parseProjectConfigFromString(newXMLPlugin);
      saveConfig();
    } else {
      throw new Exception("Could not change the name of the plugin");
    }
    saveConfig();
  }

  private void removeOldPlugin(String oldName) {
    Iterator it = project.getProjectConfig().getPluginConfigList().iterator();
    boolean found = false;
    while (it.hasNext() && !found) {
      PluginConfig pluginConfig = (PluginConfig) it.next();
      if (pluginConfig.getPluginName().equals(oldName)) {
        found = true;
        it.remove();
      }
    }
  }

  public boolean addNewPlugin(String deviceName, String className) {
    String newPlayer = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<Project name=\"" + project.getProjectName() + "\">"
            + "<Plugins>"
            + "<Plugin type=\"device\" name=\"" + deviceName + "\" class=\"" + className + "\" load=\"true\">"
            + "</Plugin>"
            + "</Plugins>"
            + "</Project>";

    boolean res = project.parseProjectConfigFromString(newPlayer);
    if (res) {
      saveConfig();
    }
    return res;
  }

  private String changeXMLAttributeNode(String xml, String tagName, String attr, String oldValue, String newValue) {
    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = null;
    Document doc;
    try {
      docBuilder = docFactory.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
    }
    try {
      doc = docBuilder.parse(new InputSource(new ByteArrayInputStream(xml.getBytes("utf-8"))));
    } catch (SAXException e) {
      e.printStackTrace();
      return xml;
    } catch (IOException e) {
      e.printStackTrace();
      return xml;
    }
    //Node firstChild = doc.getFirstChild();
    Node tag = doc.getElementsByTagName(tagName).item(0);
    // update node attribute
    NamedNodeMap attrs = tag.getAttributes();
    Node nodeAttr = attrs.getNamedItem(attr);
    if (nodeAttr != null && nodeAttr.getNodeValue().equals(oldValue)) {
      nodeAttr.setTextContent(newValue);
    }

    return getStringFromDoc(doc);

  }

  private String getStringFromDoc(org.w3c.dom.Document doc) {
    try {
      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      StreamResult result = new StreamResult(new StringWriter());
      DOMSource source = new DOMSource(doc);
      transformer.transform(source, result);
      return result.getWriter().toString();
    } catch (TransformerException ex) {
      ex.printStackTrace();
      return null;
    }
  }
}
