package de.dfki.grave.editor.action;

import de.dfki.grave.editor.panels.WorkSpace;
import de.dfki.grave.model.flow.AbstractEdge;
import de.dfki.grave.model.flow.Position;

/**
 * @author Gregor Mehlmann
 * @author Patrick Gebhard
 */
public class MoveEdgeCtrlAction extends ModifyEdgeAction {

  /** to must be in *MODEL* coordinates */
  public MoveEdgeCtrlAction(WorkSpace workSpace, AbstractEdge e, 
      boolean startCtrl, Position to) {
    super(workSpace, e);
    mWorkSpace = workSpace;
    mEdge = e;
    mNewCtrls[startCtrl ? 0 : 1] = to;
  }

  protected String msg() { return "Moving Edge Control"; }
}
