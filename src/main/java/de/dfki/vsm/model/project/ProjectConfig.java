package de.dfki.vsm.model.project;

import java.io.ByteArrayOutputStream;

import javax.xml.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.vsm.util.cpy.Copyable;

/**
 * @author Gregor Mehlmann
 */
@XmlRootElement(name="Project")
@XmlType(name="Project")
@XmlAccessorType(XmlAccessType.NONE)
public final class ProjectConfig implements Copyable {

  // The singelton logger instance
  private final Logger mLogger = LoggerFactory.getLogger(ProjectConfig.class);

  // The name of the project
  @XmlAttribute(name="name")
  private String mProjectName;

  // Construct an empty project
  public ProjectConfig() {
    // Initialize The Project Name
    mProjectName = new String();
  }

  // Construct an empty project
  public ProjectConfig(final String name) {
    // Initialize The Project Name
    mProjectName = name;
  }

  // Get the name of the project
  public final String getProjectName() {
    return mProjectName;
  }

  // Set the name of the project
  public final void setProjectName(final String name) {
    mProjectName = name;
  }

  // Get string representation
  @Override
  public final String toString() {
    final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    /*    final IOSIndentWriter writer = new IOSIndentWriter(buffer);

    try {
      //writeXML(writer);
    } catch (final XMLWriteError exc) {
      mLogger.error(exc.toString());
    }
    writer.flush();
    writer.close();*/
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
  public ProjectConfig deepCopy() {
    // TODO: Use copies of the lists
    return new ProjectConfig(mProjectName);
  }
}
