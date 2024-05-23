package de.dfki.grave.editor.project;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.grave.util.Copyable;
import de.dfki.grave.util.JaxbUtilities;

/**
 * @author Gregor Mehlmann
 */
@XmlRootElement(name="Project")
@XmlType(name="Project")
@XmlAccessorType(XmlAccessType.NONE)
public final class ProjectConfig implements Copyable {

  // The singleton logger instance
  private final static Logger mLogger = LoggerFactory.getLogger(ProjectConfig.class);

  // The name of the project
  @XmlAttribute(name="name")
  private String mProjectName;
  
  // The editor configuration
  @XmlElement(name="editorConfig")
  private EditorConfig mEditorConfig;
  
  /** Only for XML unmarshalling */
  @SuppressWarnings("unused")
  private ProjectConfig() {}
  
  // Construct an empty project
  ProjectConfig(String name, EditorConfig conf) {
    // Initialize The Project Name
    mProjectName = name;
    mEditorConfig = conf;
  }

  // Get the name of the project
  public final String getProjectName() {
    return mProjectName;
  }

  // Get the editor config of the project
  public final EditorConfig getEditorConfig() {
    return mEditorConfig;
  }
  
  // Get a copy of the project configuration
  @Override
  public ProjectConfig deepCopy() {
    return new ProjectConfig(mProjectName, mEditorConfig.copy());
  }
  
  /** Write project configuration file into the directory base.
   *
   * @param file the file to write to
   * @return true on success, false otherwise
   */
  public boolean save(final File file) {
    // Check if the configuration does exist
    if (!file.exists()) {
      try {
        file.createNewFile();
      } catch (final IOException exc) {
        // Print an error message in this case
        mLogger.error("Cannot create new project configuration file '{}'", file);
        return false;
      }
    }
    //  Write the project configuration file
    return JaxbUtilities.marshal(file, this, 
        ProjectConfig.class, EditorConfig.class);
  }
  
  /** Load project config file, return null upon failure */
  public static ProjectConfig load(File file) {
    try {
      FileInputStream in = new FileInputStream(file);
      return (ProjectConfig)JaxbUtilities.unmarshal(in, file.getAbsolutePath(),
          ProjectConfig.class, EditorConfig.class);
    }
    catch (FileNotFoundException ex) {
      mLogger.error("Project config file not found or not readable: {}", file);
    }
    return null;
  }
}
