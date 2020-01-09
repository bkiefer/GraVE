package de.dfki.grave.editor.action;

//~--- JDK imports ------------------------------------------------------------
import java.awt.Point;

import de.dfki.grave.editor.panels.WorkSpace;
import de.dfki.grave.model.flow.AbstractEdge;

/**
 * @author Gregor Mehlmann
 * @author Patrick Gebhard
 */
public class MoveEdgeCtrlAction extends ModifyEdgeAction {

  /** to must be in *MODEL* coordinates */
  public MoveEdgeCtrlAction(WorkSpace workSpace, AbstractEdge e, boolean startCtrl, Point to) {
    super(workSpace, e);
    mWorkSpace = workSpace;
    mEdge = e;
    mNewCtrls[startCtrl ? 0 : 1] = to;
  }

  protected String msg() { return "Moving Edge Control"; }
}
