package de.dfki.grave.editor.action;

import de.dfki.grave.editor.panels.WorkSpace;

/**
 *
 * @author Patrick Gebhard
 */
public class StraightenEdgeAction extends EditorAction {

  private de.dfki.grave.editor.Edge mGUIEdge = null;
  private WorkSpace mWorkSpace;

  public StraightenEdgeAction(WorkSpace workSpace, de.dfki.grave.editor.Edge edge) {
    mWorkSpace = workSpace;
    mGUIEdge = edge;
  }

  @Override
  protected void doIt() {
    mGUIEdge.straightenEdge();
  }

  @Override
  protected void undoIt() {
    // TODO Auto-generated method stub

  }

  @Override
  protected String msg() { return "Straighten Edge" ; }
}
