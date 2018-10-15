package de.dfki.vsm.model.sceneflow.glue;

import de.dfki.vsm.model.sceneflow.glue.command.Command;

public class GlueParser {
  public static Command run(String input) { return new Command(input); }
}
