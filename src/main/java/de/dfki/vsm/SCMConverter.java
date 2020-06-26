package de.dfki.vsm;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import de.dfki.grave.Constants;
import de.dfki.grave.model.flow.AbstractEdge;
import de.dfki.grave.model.flow.BasicNode;
import de.dfki.grave.model.flow.Geom;
import de.dfki.grave.model.flow.SceneFlow;
import de.dfki.grave.model.flow.SuperNode;
import de.dfki.vsm.model.sceneflow.glue.command.Command;
import de.dfki.vsm.runtime.project.RunTimeProject;

/**
 * @author Gregor Mehlmann
 */
public final class SCMConverter {
  
  private static void deleteArrows(SuperNode sc) {
    for (BasicNode n : sc.getNodes()) {
      for (AbstractEdge e : n.getEdgeList()) {
        e.setArrow(null);
      }
      if (n instanceof SuperNode) {
        deleteArrows((SuperNode)n);
      }
    }
  }

  private static void convertDir(String from, File toDir) throws IOException {
    RunTimeProject rp = new RunTimeProject();
    rp.parse(from);
    if (! toDir.exists()) {
      Files.createDirectories(toDir.toPath());
    } else {
      if (! toDir.isDirectory()) {
        System.err.println("Output path exists and is no directory!");
        System.exit(1);
      }
    }
    if (Command.convertToVOnDA) {
      Geom.initialize(32);
      // this is only the sceneflow
      String intermediate = rp.writeToString();
      ByteArrayInputStream si = new ByteArrayInputStream(
          intermediate.getBytes(StandardCharsets.UTF_8));
      SceneFlow sc = SceneFlow.loadFrom(si, from);
      // The new format needs no arrows, but we need them to construct the data
      // from the old format.
      deleteArrows(sc);
      sc.save(new File(toDir, Constants.SCENEFLOW_NAME));
    } else {
      rp.write(toDir);
    }
  }

  
  public static void main(final String[] args) throws IOException {
    int dirStart = 0;
    if (args.length > 0 && args[0].equals("-v")) {
      Command.convertToVOnDA = true;
      dirStart = 1;
    }
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
