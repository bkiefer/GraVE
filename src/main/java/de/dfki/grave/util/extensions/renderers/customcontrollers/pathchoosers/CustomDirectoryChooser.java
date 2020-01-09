package de.dfki.grave.util.extensions.renderers.customcontrollers.pathchoosers;

import java.io.File;

import javafx.stage.DirectoryChooser;

/**
 * Created by alvaro on 4/26/17.
 */
public class CustomDirectoryChooser implements PathChooser {

  private DirectoryChooser directoryChooser;

  public CustomDirectoryChooser() {
    directoryChooser = new DirectoryChooser();
  }

  @Override
  public File showDialog() {
    return directoryChooser.showDialog(null);
  }
}
