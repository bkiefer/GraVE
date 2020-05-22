package de.dfki.grave;

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

  // The logger instance of SceneMaker3
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
            editor(file);
          } else {
            error(file);
          }
        } else {
          editor();
        }
      }
    });
  }

  // Start the editor without a project
  private static void editor() {
    // Get the singelton editor instance
    final AppFrame sEditor = AppFrame.getInstance();
    // Show the singelton editor instance
    sEditor.setVisible(true);
  }

  // Start the editor with some project
  private static void editor(final File file) {
    //
    sLogger.info("Starting VSM editor with file '" + file + "'");
    // Get the singelton runtime instance
    //final RunTimeInstance sRunTime = RunTimeInstance.getInstance();
    // Get the singelton editor instance
    final AppFrame sEditor = AppFrame.getInstance();
    // Get an editor project from file
    sEditor.openProject(file.getPath());
    // Show the singelton editor instance
    sEditor.setVisible(true);
//
//         // Do something for Patrick ...
//         final Thread control = new Thread() {
//         // Termination flag
//         private boolean mDone = false;
//         // Get the input
//         private final BufferedReader mReader = new BufferedReader(
//         new InputStreamReader(System.in));
//         // Get the project
//         private final EditorProject mProject
//         = sEditor.getSelectedProjectEditor().getEditorProject();
//
//         // Set some variable on the current project
//         @Override
//         public void run() {
//         while (!mDone) {
//         // Wait until user aborts execution
//         System.err.println("Enter Command ...");
//         try {
//         final String in = mReader.readLine();
//         // Wait until user aborts execution
//         System.err.println("Your Command Is '" + in + "'");
//         if (in != null) {
//         if (in.equals("play")) {
//         // Maybe we first start the execution now ?
//         SwingUtilities.invokeLater(new Runnable() {
//
//         @Override
//         public void run() {
//         sEditor.play();
//         }
//         });
//         } else if (in.equals("stop")) {
//         // And then we stop the execution later on?
//         SwingUtilities.invokeLater(new Runnable() {
//
//         @Override
//         public void run() {
//         sEditor.stop();
//         }
//         });
//         } else if (in.equals("exit")) {
//         mDone = true;
//         } else {
//         if (sRunTime.hasVariable(mProject, "in")) {
//         // Fancy programmatic variable setting
//         sRunTime.setVariable(mProject, "in", in);
//         }
//         }
//         }
//         } catch (final IOException exc) {
//         // Do nothing
//         }
//         }
//         // Print some information
//         System.err.println("Stopping Editor Mode ...");
//         // And then cleanly exit the editor
//         SwingUtilities.invokeLater(new Runnable() {
//
//         @Override
//         public void run() {
//         // Close all projects
//         sEditor.closeAll();
//         // Dispose the editor
//         sEditor.dispose();
//         }
//         });
//         }
//         };
//         // Start the control thread
//         control.start();
//         // Print some information
//         System.err.println("Starting Editor Mode ...");

  }

  // Print usage when usage error happened
  private static void usage() {
    sLogger.error("Error: Usage: [filename]");
  }

  // Print error when a file error happened
  private static void error(final File file) {
    sLogger.error("Error: Cannot find file '" + file.getAbsolutePath() + "'");
  }
}