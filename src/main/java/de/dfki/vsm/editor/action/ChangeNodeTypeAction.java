package de.dfki.vsm.editor.action;

import de.dfki.vsm.editor.Node;
import de.dfki.vsm.editor.project.WorkSpace;
import de.dfki.vsm.model.flow.BasicNode;

/**
 * @author Patrick Gebhard
 */
public class ChangeNodeTypeAction extends EditorAction {

  private BasicNode mPrevNode = null;
  private Node mGUINode = null;

  public ChangeNodeTypeAction(WorkSpace workSpace, Node node) {
    mWorkSpace = workSpace;
    mGUINode = node;
  }

  public void doIt() {
    mPrevNode = mWorkSpace.changeType(mGUINode, mPrevNode);
  }

  public void undoIt() {
    mPrevNode = mWorkSpace.changeType(mGUINode, mPrevNode);
  }

  protected String msg() { return "Change Node Type"; }
}
