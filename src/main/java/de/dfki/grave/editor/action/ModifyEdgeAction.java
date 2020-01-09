package de.dfki.grave.editor.action;

import java.awt.Point;

import de.dfki.grave.editor.panels.WorkSpace;
import de.dfki.grave.model.flow.AbstractEdge;
import de.dfki.grave.model.flow.BasicNode;

/**
 * @author Bernd Kiefer
 *
 * An abstract superclass for all action that change an edge, in whatever way.
 * This might be a bit overgeneral, but it saves a lot of code, making
 * everything easier in the end
 */
public abstract class ModifyEdgeAction extends EditorAction {

  protected AbstractEdge mEdge;

  // All points here in model coordinates
  protected Point[] mNewCtrls;
  protected int[] mNewDocks;
  protected BasicNode[] mNewNodes;

  protected Point[] mOldCtrls;
  protected int[] mOldDocks;
  protected BasicNode[] mOldNodes;

  public ModifyEdgeAction(WorkSpace workSpace, AbstractEdge e) {
    mWorkSpace = workSpace;
    mEdge = e;
    mOldDocks = new int[]{ e.getSourceDock(), e.getTargetDock() };
    mOldCtrls = new Point[] { e.getSourceCtrlPoint().toPoint(),
        e.getTargetCtrlPoint().toPoint() };
    mOldNodes = new BasicNode[] { e.getSourceNode(), e.getTargetNode() };

    mNewDocks = new int[]{ e.getSourceDock(), e.getTargetDock() };
    mNewCtrls = new Point[] { e.getSourceCtrlPoint().toPoint(),
        e.getTargetCtrlPoint().toPoint() };
    mNewNodes = new BasicNode[] { e.getSourceNode(), e.getTargetNode() };

  }

  protected void undoIt() {
    mWorkSpace.modifyEdge(mEdge, mOldNodes, mOldDocks, mOldCtrls);
  }

  protected void doIt() {
    mWorkSpace.modifyEdge(mEdge, mNewNodes, mNewDocks, mNewCtrls);
  }

}
