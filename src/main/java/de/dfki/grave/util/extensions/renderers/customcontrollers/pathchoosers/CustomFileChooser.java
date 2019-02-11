package de.dfki.grave.util.extensions.renderers.customcontrollers.pathchoosers;

import java.io.File;

import javafx.stage.FileChooser;

/**
 * Created by alvaro on 4/26/17.
 */
public class CustomFileChooser implements PathChooser {

  private FileChooser fileChooser;

  public CustomFileChooser() {
    fileChooser = new FileChooser();
  }

  @Override
  public File showDialog() {
    return fileChooser.showOpenDialog(null);
  }
}
