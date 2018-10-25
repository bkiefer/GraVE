package de.dfki.vsm.editor.project;

import java.io.File;

import de.dfki.vsm.Preferences;
import de.dfki.vsm.model.project.EditorConfig;
import de.dfki.vsm.model.project.ProjectConfig;
import de.dfki.vsm.runtime.project.RunTimeProject;

/**
 * @author Gregor Mehlmann
 */
public class EditorProject extends RunTimeProject {

  // The editor configuration
  private EditorConfig mEditorConfig;
  // The file of the project
  private File mProjectFile;
  // The hash of the project
  private int mInitialHash;
  // Construct an editor project

  /** To load an existing project */
  public EditorProject() {
    // Initialize the project file
    mProjectFile = null;
    isNewProject = false;
  }

  /** Create brand new project */
  public EditorProject(ProjectConfig config) {
    super(config);
    this.isNewProject = true;
    this.mEditorConfig = Preferences.getPrefs().editorConfig.copy();
  }

  @Override
  public final boolean parse(final String path) {
    // Check if the file is null
    if (path == null) {
      // Print an error message
      mLogger.error("Error: Cannot parse editor project from a bad file");
      // Return false at error
      return false;
    }
    // Get the a file for this path
    final File file = new File(path);
    if (file.exists()) {
      final File base = file.getAbsoluteFile();
      if (base.exists()) {
        // First set the project file
        mProjectFile = base;
        // And then loadRunTimePlugins the project
        return parse();
      } else {
        // Print an error message
        mLogger.error("Error: Cannot find editor project directory '" + base + "'");
        // Return false at error
        return false;
      }
    } else {
      try {
        if (super.parse(path) && loadEditorConfig(path)) {
          return true;
        }
      } catch (Exception e) {
        mLogger.error("Error: Cannot find editor project directory '");
      }
      // Print an error message
      mLogger.error("Error: Cannot find editor project directory '" + file + "'");
      // Return false at error
      return false;
    }
  }

  // Save the editor project
  @Override
  public final boolean write(final File file) {
    // Check if the file is null
    if (file == null) {
      // Print an error message
      mLogger.error("Error: Cannot write editor project into a bad file");
      // Return false at error
      return false;
    }
    // Get the absolute file for the directory
    final File base = file.getAbsoluteFile();
    // Check if the project directory does exist
    if (!base.exists()) {
      // Print a warning message in this case
      mLogger.warn("Warning: Creating a new editor project directory '" + base + "'");
      // Try to create a project base directory
      if (!base.mkdir()) {
        // Print an error message
        mLogger.error("Failure: Cannot create a new editor project directory '" + base + "'");
        // Return false at error
        return false;
      }
    }
    // First set the project file
    mProjectFile = base;
    // And then save the project
    return write();
  }

  // Load the project data
  public final boolean parse() {
    // Check if the file is null
    if (mProjectFile == null) {
      // Print an error message
      mLogger.error("Error: Cannot parse editor project from a bad file");
      // Return false at error
      return false;
    }
    // Check if the project directory does exist
    if (!mProjectFile.exists()) {
      // Print an error message
      mLogger.error("Error: Cannot find editor project directory '" + mProjectFile + "'");
      // Return false at error
      return false;
    }
    // Load the project data
    if (super.parse(mProjectFile.getPath())
        && loadEditorConfig(mProjectFile.getPath())) {
      // Set the initial hash code
      mInitialHash = getHashCode();
      // Return true if project is saved
      return true;
    } else {
      // Return false when saving failed
      return false;
    }
  }

  public boolean loadEditorConfig(String path) {
    mEditorConfig = EditorConfig.load(path);
    if (mEditorConfig == null)
      mEditorConfig = Preferences.getPrefs().editorConfig.copy();
    return mEditorConfig != null;
  }

  // Save the project data
  public final boolean write() {
    // Check if the file is null
    if (mProjectFile == null) {
      // Print an error message
      mLogger.error("Error: Cannot write editor project into a bad file");
      // Return false at error
      return false;
    }
    // Check if the project directory does exist
    if (!mProjectFile.exists()) {
      // Print a warning message in this case
      mLogger.warn("Warning: Creating a new editor project directory '" + mProjectFile + "'");
      // Try to create a project base directory
      if (!mProjectFile.mkdir()) {
        // Print an error message
        mLogger.error("Failure: Cannot create a new editor project directory '" + mProjectFile + "'");
        // Return false at error
        return false;
      }
    }
    // Save the project data
    if (super.write(mProjectFile) && mEditorConfig.save(mProjectFile)) {
      // Reset the initial hash code here
      mInitialHash = getHashCode();
      // Return true when project is saved
      return true;
    } else {
      // Return false when saving failed
      return false;
    }
  }

  // Get the project base directory
  public final File getProjectFile() {
    return mProjectFile;
  }

  // Get the project pending flag
  public final boolean isPending() {
    return (mProjectFile == null);
  }

  // Get the editor configuration
  public final EditorConfig getEditorConfig() {
    return mEditorConfig;
  }

//    // Get the project file's path (moved in super class RuntimeProject (PG 11.4.2016)
//    public final String getProjectPath() {
//        if (mProjectFile != null) {
//            return mProjectFile.getPath();
//        } else {
//            return null;
//        }
//    }
  // Check if the hash code has changed
  public final boolean hasChanged() {

    // TODO: PG: DEBUG: mLogger.failure(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> HAS CHANGED was called! " + mInitialHash + " vs " + getHashCode());
    return (mInitialHash != getHashCode());
  }
}
