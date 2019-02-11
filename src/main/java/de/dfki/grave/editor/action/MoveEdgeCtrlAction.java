package de.dfki.grave.editor.action;

//~--- JDK imports ------------------------------------------------------------
import java.awt.Point;

import de.dfki.grave.editor.Edge;
import de.dfki.grave.editor.panels.WorkSpace;
import de.dfki.grave.model.flow.AbstractEdge;
import de.dfki.grave.model.flow.geom.Position;

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
    mEdge.moveCtrlPoint(mOldLocation, mStartCtrl);
  }

  protected void doIt() {
    mEdge.moveCtrlPoint(mNewLocation, mStartCtrl);
  }

  protected String msg() { return "Moving Edge Control"; }
}
