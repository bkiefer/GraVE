package de.dfki.grave.editor.action;

import de.dfki.grave.editor.Edge;
import de.dfki.grave.editor.Node;
import de.dfki.grave.editor.panels.WorkSpace;
import de.dfki.grave.model.flow.AbstractEdge;

/**
 * @author Gregor Mehlmann
 * @author Patrick Gebhard
 */
public class MoveEdgeEndPointAction extends EditorAction {

  private Edge mEdge;
  private boolean mStart;
  private int mNewDock;
  private Node mNewNode;
  private int mOldDock;
  private Node mOldNode;

  public MoveEdgeEndPointAction(WorkSpace workSpace, Edge e, boolean start,
      int dock, Node newNode) {
    mWorkSpace = workSpace;
    mEdge = e;
    AbstractEdge edge = e.getDataEdge();
    mStart = start;
    mNewDock = dock;
    mNewNode = newNode;
    mOldDock = start ? edge.getSourceDock() : edge.getTargetDock();
    mOldNode = start ? e.getSourceNode() : e.getTargetNode();
  }

  protected void undoIt() {
    mEdge.deflect(mOldNode, mOldDock, mStart);
  }

  protected void doIt() {
    mEdge.deflect(mNewNode, mNewDock, mStart);
  }

  protected String msg() { return "Moving Edge Endpoint"; }
}
