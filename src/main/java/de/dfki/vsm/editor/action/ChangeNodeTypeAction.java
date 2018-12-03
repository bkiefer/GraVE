package de.dfki.vsm.editor.action;

import de.dfki.vsm.editor.Node;
import de.dfki.vsm.editor.project.WorkSpace;

/**
 * @author Patrick Gebhard
 */
public class ChangeNodeTypeAction extends EditorAction {

  private Node mGUINode = null;

  public ChangeNodeTypeAction(WorkSpace workSpace, Node node) {
    mWorkSpace = workSpace;
    mGUINode = node;
  }

  public void doIt() {
    mWorkSpace.changeType(mGUINode);
  }

  public void undoIt() {
    mWorkSpace.changeType(mGUINode);
  }

  protected String msg() { return "Change Node Type"; }
}
