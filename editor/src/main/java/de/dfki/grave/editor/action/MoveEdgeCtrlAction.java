package de.dfki.grave.editor.action;

import de.dfki.grave.editor.panels.ProjectEditor;
import de.dfki.grave.model.AbstractEdge;
import de.dfki.grave.model.Position;

/**
 * @author Gregor Mehlmann
 * @author Patrick Gebhard
 */
public class MoveEdgeCtrlAction extends ModifyEdgeAction {

  /** to must be in *MODEL* coordinates */
  public MoveEdgeCtrlAction(ProjectEditor editor, AbstractEdge e, 
      boolean startCtrl, Position to) {
    super(editor, e);
    mEdge = e;
    mNewCtrls[startCtrl ? 0 : 1] = to;
  }

  protected String msg() { return "Moving Edge Control"; }
}
