package de.dfki.grave.model.project;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.grave.Preferences;
import de.dfki.grave.model.flow.*;

/**
 * @author Gregor Mehlmann
 */
public class EditorProject {

  private static final Logger mLogger = LoggerFactory.getLogger(EditorProject.class);

  private static final String PROJECT_CONFIG_NAME = "project.xml";
  private static final String SCENEFLOW_NAME = "sceneflow.xml";

  private String mProjectName;
  // The project Path (added PG 11.4.2016);
  private File mProjectPath = null;
  // The sceneflow of the project
  private SceneFlow mSceneFlow = null;

  // The editor configuration
  private EditorConfig mEditorConfig;

  private EditorProject(File path, String name, EditorConfig ec, SceneFlow sc) {
    mProjectPath = path;
    mProjectName = name;
    // create a new editor config with default settings (user or system)
    mEditorConfig = ec;
    mSceneFlow = sc;
  }
  
  /** Create a new EditorProject: this is a completely new project, with an
   *  yet unknown project directory.
   *
   *  Creates an initial project configuration and an empty Sceneflow.
   */
  public EditorProject(String name) {
    mProjectPath = null;
    mProjectName = name;
    // create a new editor config with default settings (user or system)
    mEditorConfig = Preferences.getPrefs().editorConfig.copy();
    mSceneFlow = new SceneFlow();
    mSceneFlow.setName(name);
  }

  /** Load an existing project, from the directory base */
  /** Load project configuration file from the directory base.
   *
   * @param base the base directory of the project
   * @return the project on success, null otherwise
   */
  public static EditorProject load(File base) {
    if (!base.isDirectory()) {
      base = base.getParentFile();
    }
    SceneFlow sc = SceneFlow.load(new File(base, SCENEFLOW_NAME));
    if (sc == null)
      return null;
    ProjectConfig pc = ProjectConfig.load(new File(base, PROJECT_CONFIG_NAME));
    String name;
    EditorConfig ec;
    if (pc == null) {
      mLogger.warn("Missing project config file {}, using defaults",
          PROJECT_CONFIG_NAME);
      name = base.getName();
      ec = Preferences.getPrefs().editorConfig.copy();
    } else {
      name = pc.getProjectName();
      ec = pc.getEditorConfig();
      if (name == null || ec == null) {
        mLogger.error("Corrupt project config file {}, delete it!",
            PROJECT_CONFIG_NAME);
        return null;
      }
    }
    return new EditorProject(base, name, ec, sc);
  }
  
  // Get the path of the project (added PG 11.4.2016)
  public final File getProjectPath() {
    return mProjectPath;
  }

  public boolean isNew() {
    return mProjectPath == null;
  }
  
  // Get the name of the project's configuration
  public final String getProjectName() {
    return mProjectName;
  }

  // Set the name in the project's configuration
  public final void setProjectName(final String name) {
    mProjectName = name;
  }
  
  // Get the sceneflow of the project
  public final SceneFlow getSceneFlow() {
    return mSceneFlow;
  }

  /** Get the editor configuration of this project */
  public final EditorConfig getEditorConfig() {
    return mEditorConfig;
  }
  
  /** Set the editor configuration of this project: to enable cancelling
   *  of changes
   */
  public final void setEditorConfig(EditorConfig conf) {
    mEditorConfig = conf;
  }

  /** Save only the editor configuration */
  public boolean saveEditorConfig() {
    if (mProjectPath == null) return false;
    return writeProjectConfig(mProjectPath);
  }
  
  public boolean hasChanged() {
    // TODO: DO SOMETHING MEANINGFUL HERE, BUT ALWAYS BE ON THE SAFE SIDE
    return true;
  }

  /** Write project configuration file into the directory base.
   *
   * @param base the base directory of the project
   * @return true on success, false otherwise
   */
  private boolean writeProjectConfig(final File base) {
    ProjectConfig conf = new ProjectConfig(mProjectName, mEditorConfig);
    File file = new File(base, PROJECT_CONFIG_NAME);
    boolean result;
    if (result = conf.save(file)) {
      mLogger.info("Saved project configuration to file '{}'", file);
    } else {
      mLogger.error("Cannot write project configuration to file '{}'", file);
    }
    return result;
  }

  private boolean writeSceneFlow(final File base) {
    // Create the sceneflow configuration file
    final File file = new File(base, SCENEFLOW_NAME);
    return mSceneFlow.save(file);
  }

  /** Save a *new* project file, i.e., where the base directory is not yet 
   *  specified, and now put into parentDirectory. Can also be used for
   *  "Save As"
   * @param parentDirectory
   * @return true on success, false otherwise
   */
  public boolean saveNewProject(File parentDirectory) {
    if (! parentDirectory.exists() || !parentDirectory.isDirectory()) {
      mLogger.error("{} does not exist or is not a directory.", parentDirectory);
      return false;
    }
    File projectDir = new File(parentDirectory, mProjectName);
    if (! projectDir.mkdir()) {
      mLogger.error("directory {} could not be created.", projectDir);
      return false;
    }
    mProjectPath = projectDir;
    if (saveProject()) return true;
    mProjectPath = null;
    return false;
  }

  /** Save a project file that was loaded from file */
  public boolean saveProject() {
    assert(mProjectPath != null);
    return writeProjectConfig(mProjectPath) && writeSceneFlow(mProjectPath); 
  }

}
