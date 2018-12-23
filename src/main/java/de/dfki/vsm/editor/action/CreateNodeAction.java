package de.dfki.vsm.editor.action;

//~--- JDK imports ------------------------------------------------------------
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import de.dfki.vsm.editor.Node;
import de.dfki.vsm.editor.project.WorkSpace;
import de.dfki.vsm.model.flow.BasicNode;

/**
 * @author Gregor Mehlmann
 * @author Patrick Gebhard
 */
public class CreateNodeAction extends EditorAction {

  private Point mCoord;
  BasicNode mDataNode;
  private Collection<Node> mNode = new ArrayList<Node>();

  public CreateNodeAction(WorkSpace workSpace, Point coordinate,
      BasicNode node) {
    mWorkSpace = workSpace;
    mCoord = coordinate;
    mDataNode = node;
  }

  protected void undoIt() {
    mWorkSpace.removeNodes(false, mNode);
  }

  protected void doIt() {
    if (mNode.isEmpty()) {
      mNode.add(mWorkSpace.createNode(mCoord, mDataNode));
      mWorkSpace.addNewNode(mNode.iterator().next());
    } else {
      mWorkSpace.pasteNodesAndEdges(mNode, Collections.emptyList());
    }
  }

  protected String msg() { return "Creation Of Node"; }
}
