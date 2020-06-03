package de.dfki.grave.editor.panels;

import java.io.File;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileView;

import de.dfki.grave.model.project.EditorProject;

public final class OpenProjectView extends FileView {
  // The Icon To Show For Project Directories

  ImageIcon sceneMakerIcon = new ImageIcon("res/img/icon.png");
  //

  @Override
  public final Icon getIcon(final File file) {
    // The Icon
    Icon icon = null;

    if (file.isDirectory()) {
      File configFile = new File(file.getAbsolutePath() + System.getProperty("file.separator") + "project.xml");

      if (configFile.exists()) {
        icon = sceneMakerIcon;
      }
    }

    return icon;
  }

  /** Return the name of the project, which is the name of the directory */
  public String getName(File f) {
    assert(f.isDirectory());
    return f.getName();
  }

  public static boolean isAcceptedFile(File f) {
    if (f.isDirectory()) {

      if (isVSMProject(f)) {
        return true;
      }

      for (File listOfFile : getListOfFiles(f)) {
        if (listOfFile.isDirectory()) {
          return true;
        }
      }
    }

    return false;
  }

  private static boolean isVSMProject(File f) {
    File configFile = new File(f.getPath() + System.getProperty("file.separator") + "project.xml");
    return configFile.exists();
  }

  private static File[] getListOfFiles(File f) {
    File[] listOfFiles = f.listFiles();
    return listOfFiles != null ? listOfFiles : new File[0];
  }
}
