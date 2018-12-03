package de.dfki.vsm.editor.action;

//~--- JDK imports ------------------------------------------------------------
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;

//~--- non-JDK imports --------------------------------------------------------
import de.dfki.vsm.editor.Edge;
import de.dfki.vsm.editor.Node;
import de.dfki.vsm.editor.project.WorkSpacePanel;
import de.dfki.vsm.model.flow.AbstractEdge;

/**
 * @author Patrick Gebhard
 */
public class DeflectEdgeAction extends EditorAction {

  private Edge mGUIEdge;
  private Node mSourceGUINode, mTargetGUINode;
  private Point mDropPoint;

  private Edge mNewEdge;


  public DeflectEdgeAction(WorkSpacePanel workSpace, Edge edge, Node newTargetNode, Point newDropPoint) {
    mWorkSpace = workSpace;
    mGUIEdge = edge;
    mSourceGUINode = edge.getSourceNode();
    mTargetGUINode = newTargetNode;
    mDropPoint = newDropPoint;
  }

  public void doIt() {
    mWorkSpace.removeEdges(new ArrayList<Edge>(){{add(mGUIEdge);}});
    AbstractEdge mDataEdge = mGUIEdge.getDataEdge();
    // connect to the new node
    mDataEdge.connect(mSourceGUINode.getDataNode(), mTargetGUINode.getDataNode());

    // create a new gui edge
    mNewEdge = new Edge(mWorkSpace, mDataEdge, mSourceGUINode, mTargetGUINode,
        mSourceGUINode.getEdgeDockPoint(mNewEdge), mDropPoint);
    mNewEdge.straightenEdge();
    mWorkSpace.pasteNodesAndEdges(Collections.emptyList(),
        new ArrayList<Edge>(){{add(mNewEdge);}});

  }

  public void undoIt() {
    mWorkSpace.removeEdges(new ArrayList<Edge>(){{add(mNewEdge);}});
    mWorkSpace.addEdges(new ArrayList<Edge>(){{add(mGUIEdge);}});
    mNewEdge = null;
  }

  @Override
  public String msg() {
    return "Undo Change Edge Source/Target";
  }
}
