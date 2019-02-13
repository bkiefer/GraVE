package de.dfki.grave.editor.action;

import java.util.ArrayList;
import java.util.List;

import de.dfki.grave.editor.Node;
import de.dfki.grave.editor.panels.WorkSpace;

/**
 * @author Gregor Mehlmann
 * @author Patrick Gebhard
 */
public class CreateNodeAction extends EditorAction {

  private List<Node> mNode = new ArrayList<Node>();

  public CreateNodeAction(WorkSpace workSpace, Node node) {
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
