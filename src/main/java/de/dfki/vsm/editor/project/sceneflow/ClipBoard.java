package de.dfki.vsm.editor.project.sceneflow;

import java.util.HashSet;

import de.dfki.vsm.model.flow.BasicNode;

/**
 * Created by alvaro on 7/18/16.
 * The clipboard should be shared among the different projects
 *
 */
@SuppressWarnings("serial")
public class ClipBoard extends HashSet<BasicNode> {

  private static ClipBoard sInstance;

  private ClipBoard() {
  }

  public static ClipBoard getsInstance() {
    if (sInstance == null) {
      sInstance = new ClipBoard();
    }
    return sInstance;
  }
}
