package de.dfki.vsm.editor.action;

import de.dfki.vsm.editor.Node;
import de.dfki.vsm.editor.project.WorkSpace;
import de.dfki.vsm.model.flow.BasicNode;

/**
 * @author Patrick Gebhard
 */
public class ToggleStartNodeAction extends EditorAction {

  private BasicNode oldStartNode;
  private Node mGUINode;

  public ToggleStartNodeAction(WorkSpace workSpace, Node node) {
    mWorkSpace = workSpace;
    mGUINode = node;
    oldStartNode = mGUINode.getDataNode().getParentNode().getStartNode();
  }

  @Override
  protected void doIt() {
    BasicNode mDataNode = mGUINode.getDataNode();
    mDataNode.getParentNode().setStartNode(mDataNode);
  }

  @Override
  public void undoIt() {
    BasicNode mDataNode = mGUINode.getDataNode();
    mDataNode.getParentNode().setStartNode(oldStartNode);
  }

  @Override
  public String msg() {
    return "Toggle Node Being Start Node";
  }
}
