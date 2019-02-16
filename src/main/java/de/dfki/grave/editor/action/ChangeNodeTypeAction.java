package de.dfki.grave.editor.action;

import de.dfki.grave.editor.Node;
import de.dfki.grave.editor.panels.WorkSpace;
import de.dfki.grave.model.flow.BasicNode;

/**
 * @author Patrick Gebhard
 */
public class ChangeNodeTypeAction extends EditorAction {

  private BasicNode mPrevNode = null;
  private BasicNode mNode = null;

  public ChangeNodeTypeAction(WorkSpace workSpace, BasicNode node) {
    mWorkSpace = workSpace;
    mNode = node;
  }

  public void doIt() {
    mPrevNode = mWorkSpace.changeType(mNode, mPrevNode);
  }

  public void undoIt() {
    mPrevNode = mWorkSpace.changeType(mNode, mPrevNode);
  }

  protected String msg() { return "Change Node Type"; }
}
