package de.dfki.vsm.editor.action;

//~--- JDK imports ------------------------------------------------------------
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import de.dfki.vsm.editor.Node;
import de.dfki.vsm.editor.project.WorkSpace;

/**
 * @author Gregor Mehlmann
 * @author Patrick Gebhard
 */
public class CreateNodeAction extends EditorAction {

  private Point mCoord;
  Node.Type mType;
  private Collection<Node> mNode = new ArrayList<Node>();

  public CreateNodeAction(WorkSpace workSpace, Point coordinate, Node.Type type) {
    mWorkSpace = workSpace;
    mCoord = coordinate;
    mType = type;
  }

  protected void undoIt() {
    mWorkSpace.removeNodes(false, mNode);
  }

  protected void doIt() {
    if (mNode.isEmpty()) {
      mNode.add(mWorkSpace.createNode(mCoord, mType));
      mWorkSpace.addNewNode(mNode.iterator().next());
    } else {
      mWorkSpace.pasteNodesAndEdges(mNode, Collections.emptyList());
    }
  }

  protected String msg() { return "Creation Of Node"; }
}
