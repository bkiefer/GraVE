package de.dfki.vsm.editor.action;

import de.dfki.vsm.editor.Edge;
import de.dfki.vsm.editor.Node;
import de.dfki.vsm.editor.project.WorkSpace;
import de.dfki.vsm.model.flow.AbstractEdge;

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
