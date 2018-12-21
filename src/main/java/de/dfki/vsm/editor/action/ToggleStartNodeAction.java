package de.dfki.vsm.editor.action;

import de.dfki.vsm.editor.Node;
import de.dfki.vsm.editor.project.WorkSpace;
import de.dfki.vsm.model.flow.BasicNode;

/**
 * @author Patrick Gebhard
 */
public class ToggleStartNodeAction extends EditorAction {

  private Node mGUINode;

  public ToggleStartNodeAction(WorkSpace workSpace, Node node) {
    mWorkSpace = workSpace;
    mGUINode = node;
  }

  @Override
  protected void doIt() {
    BasicNode mDataNode = mGUINode.getDataNode();
    if (mDataNode.isStartNode()) {
      mGUINode.removeStartSign();
      mDataNode.getParentNode().removeStartNode(mDataNode);
    } else {
      mDataNode.getParentNode().addStartNode(mDataNode);
      mGUINode.addStartSign();
    }
  }

  @Override
  public void undoIt() {
    doIt();
  }

  @Override
  public String msg() {
    return "Toggle Node Being Start Node";
  }
}
