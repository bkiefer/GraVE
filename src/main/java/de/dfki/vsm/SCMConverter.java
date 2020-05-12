package de.dfki.vsm;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import de.dfki.vsm.model.sceneflow.glue.command.Command;
import de.dfki.vsm.runtime.project.RunTimeProject;

/**
 * @author Gregor Mehlmann
 */
public final class SCMConverter {

    // Start SceneMaker3 in a specific mode
    public static void main(final String[] args) throws IOException {
      if (args.length > 0 && args[0].equals("-v")) {
        Command.convertToVOnDA = true;
      }
      if (! (new File("project.xml").exists())) {
    	  try (FileWriter out = new FileWriter("project.xml")) {
    		  out.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
    		  		+ "<Project name=\"switch\"/>");
    	  }
      }
      Preferences.parseConfigFile();
      RunTimeProject rp = new RunTimeProject();
      rp.parse(".");
      File toDir = new File("./out/");
      //Files.createDirectory(toDir.toPath());
      rp.write(toDir);
    }
}
