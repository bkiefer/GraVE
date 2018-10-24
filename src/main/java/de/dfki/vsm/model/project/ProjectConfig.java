package de.dfki.vsm.model.project;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import de.dfki.vsm.model.ModelObject;
import de.dfki.vsm.util.ios.IOSIndentWriter;
import de.dfki.vsm.util.xml.XMLParseAction;
import de.dfki.vsm.util.xml.XMLParseError;
import de.dfki.vsm.util.xml.XMLWriteError;

/**
 * @author Gregor Mehlmann
 */
public final class ProjectConfig implements ModelObject {

  // The singelton logger instance
  private final Logger mLogger
          = LoggerFactory.getLogger(ProjectConfig.class);;
  // The name of the project
  private String mProjectName;
  // The list of plugin configurations
  private final ArrayList<PluginConfig> mPluginList;

  // Construct an empty project
  public ProjectConfig() {
    // Initialize The Project Name
    mProjectName = new String();
    // Initialize The Plugin List
    mPluginList = new ArrayList<>();
  }

  // Construct an empty project
  public ProjectConfig(final String name,
          final ArrayList<PluginConfig> plugins,
          final ArrayList<AgentConfig> agents) {
    // Initialize The Project Name
    mProjectName = name;
    // Initialize The Plugin List
    mPluginList = plugins;
  }

  // Get the name of the project
  public final String getProjectName() {
    return mProjectName;
  }

  // Set the name of the project
  public final void setProjectName(final String name) {
    mProjectName = name;
  }

  public final PluginConfig getPluginConfig(final String name) {
    for (final PluginConfig config : mPluginList) {
      if (config.getPluginName().equals(name)) {
        return config;
      }
    }
    return null;
  }

  // Get the list of plugin configurations
  public ArrayList<PluginConfig> getPluginConfigList() {
    return mPluginList;
  }

  // Write the project configuration
  @Override
  public final void writeXML(final IOSIndentWriter stream) throws XMLWriteError {
    stream.println("<Project name=\"" + mProjectName + "\">");
    stream.push();
    // Write the plugin configurations
    stream.println("<Plugins>").push();
    for (final PluginConfig plugin : mPluginList) {
      plugin.writeXML(stream);
    }
    stream.pop().println("</Plugins>");
    // Write the agent configurations
    stream.pop().print("</Project>").flush();
  }

  public boolean deleteDevice(PluginConfig plugin) {
    return mPluginList.remove(plugin);
  }

  // Parse the project configuration
  @Override
  public final void parseXML(final Element element) throws XMLParseError {
    // Get The Type Of The Config
    final String tag = element.getTagName();
    // Check The Type Of The Config
    if (tag.equals("Project")) {
      // Get The Project Name
      mProjectName = element.getAttribute("name");
      // Parse The Individual Entries
      XMLParseAction.processChildNodes(element, new XMLParseAction() {
        @Override
        public void run(final Element element) throws XMLParseError {
          // Get The Tag Name
          final String tag = element.getTagName();
          // Check The Tag Name
          if (tag.equals("Plugins")) {
            XMLParseAction.processChildNodes(element, "Plugin", new XMLParseAction() {
              @Override
              public void run(Element element) throws XMLParseError {
                // Create A New Project Plugin
                final PluginConfig plugin = new PluginConfig();
                // And Parse The Project Plugin
                plugin.parseXML(element);
                // And Add It To The Plugin List
                mPluginList.add(plugin);
              }
            });
          }
        }
      });
    }
  }

  // Get string representation
  @Override
  public final String toString() {
    final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    final IOSIndentWriter writer = new IOSIndentWriter(buffer);
    try {
      writeXML(writer);
    } catch (final XMLWriteError exc) {
      mLogger.error(exc.toString());
    }
    writer.flush();
    writer.close();
    try {
      //return buffer.toString("UTF-8");
      return buffer.toString();
    } catch (final Exception exc) {
      exc.printStackTrace();
      //
      return null;
    }
  }

  // Get a copy of the project configuration
  @Override
  public ProjectConfig getCopy() {
    // TODO: Use copies of the lists
    return new ProjectConfig(mProjectName, null, null);
  }
}
