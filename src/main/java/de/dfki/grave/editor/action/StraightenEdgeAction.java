package de.dfki.grave.editor.action;

import de.dfki.grave.editor.panels.WorkSpace;
import de.dfki.grave.model.flow.AbstractEdge;

/**
 *
 * @author Patrick Gebhard
 */
public class StraightenEdgeAction extends ReshapeEdgeAction{

  public StraightenEdgeAction(WorkSpace workSpace, AbstractEdge edge) {
    super(workSpace, edge);
  }

  protected void reshape() { mWorkSpace.straightenEdge(mEdge); }

  @Override
  protected String msg() { return "Straighten Edge" ; }
}
