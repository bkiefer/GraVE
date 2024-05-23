package de.dfki.grave.app;

import java.io.File;

import javax.swing.SwingUtilities;

//import de.dfki.vsm.runtime.RunTimeInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.grave.model.flow.Geom;

/**
 * @author Gregor Mehlmann
 */
public final class MainGrave {

  private final static Logger sLogger = LoggerFactory.getLogger(MainGrave.class);

  // Start SceneMaker3 in a specific mode
  public static void main(final String[] args) {
    Geom.initialize(32);
    // Let Java Swing do the work for us
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public final void run() {
        // Check if we have at least one argument
        if (args.length > 0) {
          // Get the project file name agrument
          final String name = args[0];
          // Create the project configuration
          final File file = new File(name);
          if (file.exists()) {
            sLogger.info("Starting editor with file '{}'", file);
            final AppFrame sEditor = AppFrame.getInstance();
            // Get an editor project from file
            sEditor.openProject(file);
            // Show the singelton editor instance
            sEditor.setVisible(true);
          } else {
            sLogger.error("Error: Cannot find file '{}'", file.getAbsolutePath());
          }
        } else {
          // Start the editor without a project
          // Get the singleton app instance
          final AppFrame sEditor = AppFrame.getInstance();
          sEditor.setVisible(true);
        }
      }
    });
  }

  // Print usage when usage error happened
  private static void usage() {
    sLogger.error("Error: Usage: [filename]");
  }
}
