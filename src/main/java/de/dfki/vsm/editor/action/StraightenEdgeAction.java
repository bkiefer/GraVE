package de.dfki.vsm.editor.action;

import de.dfki.vsm.editor.project.WorkSpace;

/**
 *
 * @author Patrick Gebhard
 */
public class StraightenEdgeAction extends EditorAction {

  private de.dfki.vsm.editor.Edge mGUIEdge = null;
  private WorkSpace mWorkSpace;

  public StraightenEdgeAction(WorkSpace workSpace, de.dfki.vsm.editor.Edge edge) {
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
