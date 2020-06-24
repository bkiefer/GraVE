package de.dfki.vsm;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import de.dfki.vsm.model.sceneflow.glue.command.Command;
import de.dfki.vsm.runtime.project.RunTimeProject;

/**
 * @author Gregor Mehlmann
 */
public final class SCMConverter {

  private static void convertDir(String from, File toDir) throws IOException {
    RunTimeProject rp = new RunTimeProject();
    rp.parse(from);
    Files.createDirectories(toDir.toPath());
    rp.write(toDir);
  }

    // Start SceneMaker3 in a specific mode
    public static void main(final String[] args) throws IOException {
      int dirStart = 0;
      if (args.length > 0 && args[0].equals("-v")) {
        Command.convertToVOnDA = true;
        dirStart = 1;
      }
      /*
      if (! (new File("project.xml").exists())) {
    	  try (FileWriter out = new FileWriter("project.xml")) {
    		  out.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
    		  		+ "<Project name=\"switch\"/>");
    	  }
      }
      */
      Preferences.parseConfigFile();
      if (dirStart == args.length) {
        convertDir(".", new File("out/"));
      } else {
        for (int i = dirStart; i < args.length; ++i) {
          convertDir(args[i], new File("out/" + args[i] + "/"));
        }
      }
    }
}
