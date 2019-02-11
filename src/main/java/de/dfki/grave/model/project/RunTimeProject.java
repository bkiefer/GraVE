package de.dfki.grave.model.project;

import java.io.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.grave.model.flow.AbstractEdge;
import de.dfki.grave.model.flow.EpsilonEdge;
import de.dfki.grave.model.flow.SceneFlow;
import de.dfki.grave.model.flow.TimeoutEdge;
import de.dfki.grave.util.JaxbUtilities;

/**
 * @author Gregor Mehlmann
 */
public class RunTimeProject {

  protected boolean isNewProject = false;
  // The singleton logger instance
  protected final Logger mLogger
          = LoggerFactory.getLogger(RunTimeProject.class);

  // The project Path (added PG 11.4.2016);
  private String mProjectPath = "";
  // The sceneflow of the project
  private SceneFlow mSceneFlow = null;
  // The project configuration of the project
  private ProjectConfig mProjectConfig = null;

  /** To load the project config afterwards */
  protected RunTimeProject() { }

  // Construct an empty runtime project: For newProject
  public RunTimeProject(ProjectConfig config) {
    mProjectConfig = config;
    mSceneFlow = new SceneFlow();
  }

  public boolean isNewProject() {
    return isNewProject;
  }

  // Get the path of the project (added PG 11.4.2016)
  public final String getProjectPath() {
    return mProjectPath;
  }

  public void setProjectPath(String s) {
    mProjectPath = s;
  }

  // Get the name of the project's configuration
  public final String getProjectName() {
    return mProjectConfig.getProjectName();
  }

  // Set the name in the project's configuration
  public final void setProjectName(final String name) {
    mProjectConfig.setProjectName(name);
  }

  // Get the sceneflow of the project
  public final SceneFlow getSceneFlow() {
    return mSceneFlow;
  }

  // Get the project configuration (added PG 15.4.2016)
  public final ProjectConfig getProjectConfig() {
    return mProjectConfig;
  }

  public boolean parse(final String file) {
    // Check if the file is null
    if (file == null) {
      // Print an error message
      mLogger.error("Error: Cannot parse project: bad file");
      // Return false at error
      return false;
    }
    // remember Path (e.g. EditorProject calls this without instantiation of
    // the RunTimeProject class, so mProjectPath is (re)set her (PG 11.4.2016)
    mProjectPath = file;

    // Parse the project from file
    return parseProjectConfig(file) && parseSceneFlow(file);
  }

  // Write the project data to a directory
  public boolean write(final File file) {
    // Check if the file is null
    if (file == null) {
      // Print an error message
      mLogger.error("Error: Cannot write runtime project into a bad file");
      // Return false at error
      return false;
    }
    // Get the absolute file for the directory
    final File base = file.getAbsoluteFile();
    // Check if the project directory does exist
    if (!base.exists()) {
      // Print a warning message in this case
      mLogger.warn("Warning: Creating a new runtime project directory '" + base + "'");
      // Try to create a project base directory
      if (!base.mkdir()) {
        // Print an error message
        mLogger.error("Failure: Cannot create a new runtime project directory '" + base + "'");
        // Return false at error
        return false;
      }
    }
    if (mProjectPath.equals("")) {
      mProjectPath = file.getPath();
    }
    // Save the project to the base directory
    return (writeProjectConfig(base) && writeSceneFlow(base));
  }

  // Load the executors of the project
  // TODO: Load Plugins and call methods on them via the evaluator in the interpreter
  // Make a new command type in the syntax for that purpose
  public final Object call(final String name, final String method) {
    return null;
  }

  private boolean parseProjectConfig(final String path) {
    InputStream inputStream = null;
    final File file = new File(path, "project.xml");
    if (file.exists()) {
      try {
        inputStream = new FileInputStream(file);
      } catch (FileNotFoundException e) {
        mLogger.error("Error: Cannot find project configuration file '" + file + "'");
      }
    } else {
      inputStream = ClassLoader.getSystemResourceAsStream(path + System.getProperty("file.separator") + "project.xml");
      if (inputStream == null) {
        // Print an error message in this case
        mLogger.error("Error: Cannot find project configuration file  ");
        // Return failure if it does not exist
        return false;
      }

    }

    if ((mProjectConfig =
        (ProjectConfig)JaxbUtilities.unmarshal(inputStream, path, ProjectConfig.class)) == null) {
      mLogger.error("Error: Cannot parse project configuration file  in path" + path);
      return false;
    }

    mLogger.info("Loaded project from path '" + path + "':\n" + mProjectConfig);
    // Return success if the project was loaded
    return true;
  }

  public boolean parseProjectConfigFromString(String xml) {
    //Parse the config file for project from a string
    InputStream stream = new ByteArrayInputStream(xml.getBytes());
    mProjectConfig = (ProjectConfig)
        JaxbUtilities.unmarshal(stream, "", ProjectConfig.class);
    return (mProjectConfig != null);
  }

  private boolean writeProjectConfig(final File base) {
    // Create the project configuration file
    final File file = new File(base, "project.xml");
    // Check if the configuration does exist
    if (!file.exists()) {
      // Print a warning message in this case
      mLogger.warn("Warning: Creating the new project configuration file '" + file + "'");
      // Create a new configuration file now
      try {
        // Try to create a new configuration file
        if (!file.createNewFile()) {
          // Print an error message in this case
          mLogger.warn("Warning: There already exists a project configuration file '" + file + "'");
        }
      } catch (final IOException exc) {
        // Print an error message in this case
        mLogger.error("Failure: Cannot create the new project configuration file '" + file + "'");
        // Return failure if it does not exist
        return false;
      }
    }
    // Write the project configuration file
    if (!JaxbUtilities.marshal(file, mProjectConfig, ProjectConfig.class)) {
      // Print an error message in this case
      //mLogger.error("Error: Cannot write project configuration file '" + file + "'");
      // Return failure if it does not exist
      return false;
    }
    // Print an information message in this case
    mLogger.info("Saved project configuration file '" + file + "':\n" + mProjectConfig);
    // Return success if the project was saved
    return true;
  }

  private boolean parseSceneFlow(final String path) {
    InputStream inputStream = null;
    final File file = new File(path, "sceneflow.xml");
    // Check if the configuration file does exist
    if (file.exists()) {
      try {
        inputStream = new FileInputStream(file);
      } catch (FileNotFoundException e) {
        mLogger.error("Error: Cannot find sceneflow configuration file '" + file + "'");
      }
    } else {
      inputStream = ClassLoader.getSystemResourceAsStream(path + System.getProperty("file.separator") + "sceneflow.xml");
      if (inputStream == null) {
        // Print an error message in this case
        mLogger.error("Error: Cannot find sceneflow configuration file   project ");
        // Return failure if it does not exist
        return false;
      }

    }
    mSceneFlow = (SceneFlow) JaxbUtilities.unmarshal(inputStream, path,
        SceneFlow.class, AbstractEdge.class, TimeoutEdge.class, EpsilonEdge.class );

    // Perform all the postprocessing steps
    mSceneFlow.establishParentNodes();
    mSceneFlow.establishStartNodes();
    mSceneFlow.establishTargetNodes();
    // Print an information message in this case
    //mLogger.message("Loaded sceneflow configuration file in path '" + path + "'");
    // Return success if the project was loaded
    return true;

  }

  private boolean writeSceneFlow(final File base) {
    // Create the sceneflow configuration file
    final File file = new File(base, "sceneflow.xml");
    // Check if the configuration file does exist
    if (!file.exists()) {
      // Print a warning message in this case
      mLogger.warn("Warning: Creating the new sceneflow configuration file '" + file + "'");
      // Create a new configuration file now
      try {
        // Try to create a new configuration file
        if (!file.createNewFile()) {
          // Print an error message in this case
          mLogger.warn("Warning: There already exists a sceneflow configuration file '" + file + "'");
        }
      } catch (final IOException exc) {
        // Print an error message in this case
        mLogger.error("Failure: Cannot create the new sceneflow configuration file '" + file + "'");
        // Return failure if it does not exist
        return false;
      }
    }
    // Write the sceneflow configuration file
    JaxbUtilities.marshal(file, mSceneFlow, SceneFlow.class);
    // Print an information message in this case
    //mLogger.message("Saved sceneflow configuration file '" + file + "':\n" + mSceneFlow);
    // Return success if the project was saved
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 59 * hash + (this.isNewProject ? 1 : 0);
    hash = 59 * hash + this.mProjectPath.hashCode();
    hash = mSceneFlow != null? 59 * hash + this.mSceneFlow.hashCode() : hash;
    hash = mProjectConfig != null? 59 * hash + this.mProjectConfig.hashCode() : hash;
    return hash;
  }

}
