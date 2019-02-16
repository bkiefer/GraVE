package de.dfki.grave.editor.action;

import java.util.ArrayList;
import java.util.List;

import de.dfki.grave.editor.panels.WorkSpace;
import de.dfki.grave.model.flow.BasicNode;

/**
 * @author Gregor Mehlmann
 * @author Patrick Gebhard
 */
public class CreateNodeAction extends EditorAction {

  private List<BasicNode> mNode = new ArrayList<BasicNode>();

  public CreateNodeAction(WorkSpace workSpace, BasicNode node) {
    mWorkSpace = workSpace;
    mNode.add(node);
  }

  protected void undoIt() {
    mWorkSpace.removeNodes(false, mNode);
  }

  protected void doIt() {
    mWorkSpace.addNode(mNode.get(0));
  }

  protected String msg() { return "Creation Of Node"; }
}
