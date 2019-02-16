package de.dfki.grave.editor.action;

import de.dfki.grave.editor.panels.WorkSpace;
import de.dfki.grave.model.flow.BasicNode;

/**
 * @author Patrick Gebhard
 */
public class ToggleStartNodeAction extends EditorAction {

  private BasicNode oldStartNode;
  private BasicNode mNode;

  public ToggleStartNodeAction(WorkSpace workSpace, BasicNode node) {
    mWorkSpace = workSpace;
    mNode = node;
    oldStartNode = mNode.getParentNode().getStartNode();
  }

  @Override
  protected void doIt() {
    BasicNode mDataNode = mNode;
    mDataNode.getParentNode().setStartNode(mDataNode);
  }

  @Override
  public void undoIt() {
    BasicNode mDataNode = mNode;
    mDataNode.getParentNode().setStartNode(oldStartNode);
  }

  @Override
  public String msg() {
    return "Toggle Node Being Start Node";
  }
}
