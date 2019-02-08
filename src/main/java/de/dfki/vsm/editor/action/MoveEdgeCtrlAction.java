package de.dfki.vsm.editor.action;

//~--- JDK imports ------------------------------------------------------------
import java.awt.Point;

import de.dfki.vsm.editor.Edge;
import de.dfki.vsm.editor.project.WorkSpace;
import de.dfki.vsm.model.flow.AbstractEdge;
import de.dfki.vsm.model.flow.geom.Position;

/**
 * @author Gregor Mehlmann
 * @author Patrick Gebhard
 */
public class MoveEdgeCtrlAction extends EditorAction {

  private Edge mEdge;
  private boolean mStartCtrl;
  private Point mNewLocation;
  private Point mOldLocation;

  public MoveEdgeCtrlAction(WorkSpace workSpace, Edge e, boolean startCtrl, Point to) {
    mWorkSpace = workSpace;
    mEdge = e;
    mStartCtrl = startCtrl;
    mNewLocation = to;
    AbstractEdge edge = mEdge.getDataEdge();
    Position old = mStartCtrl
        ? edge.getSourceCtrlPoint() : edge.getTargetCtrlPoint();
    mOldLocation = new Point(old.getXPos(), old.getYPos());
  }

  protected void undoIt() {
    AbstractEdge edge = mEdge.getDataEdge();
    if (mStartCtrl)
      edge.setSourceCtrlPoint(mOldLocation);
    else
      edge.setTargetCtrlPoint(mOldLocation);
    mEdge.updateEdgeGraphics();
  }

  protected void doIt() {
    AbstractEdge edge = mEdge.getDataEdge();
    if (mStartCtrl)
      edge.setSourceCtrlPoint(mNewLocation);
    else
      edge.setTargetCtrlPoint(mNewLocation);
    mEdge.updateEdgeGraphics();
  }

  protected String msg() { return "Moving Edge Control"; }
}
