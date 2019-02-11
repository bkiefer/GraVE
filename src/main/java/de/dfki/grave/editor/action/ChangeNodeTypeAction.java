package de.dfki.grave.editor.action;

import de.dfki.grave.editor.Node;
import de.dfki.grave.editor.panels.WorkSpace;
import de.dfki.grave.model.flow.BasicNode;

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
