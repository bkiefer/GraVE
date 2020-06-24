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

    // Start SceneMaker3 in a specific mode
    public static void main(final String[] args) throws IOException {
      int dirStart = 0;
      if (args.length > 0 && args[0].equals("-v")) {
        Command.convertToVOnDA = true;
        dirStart = 1;
      }
      if (! (new File("project.xml").exists())) {
    	  try (FileWriter out = new FileWriter("project.xml")) {
    		  out.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
    		  		+ "<Project name=\"switch\"/>");
    	  }
      }
      Preferences.parseConfigFile();
      RunTimeProject rp = new RunTimeProject();
      if (dirStart == args.length) {
        rp.parse(".");
        File toDir = new File("out/");
        Files.createDirectories(toDir.toPath());
        rp.write(toDir);
      } else {
        for (int i = dirStart; i < args.length; ++i) {
          rp.parse(args[i]);
          File toDir = new File("out/" + args[i] + "/");
          Files.createDirectories(toDir.toPath());
          rp.write(toDir);
        }
      }                 
    }
}
