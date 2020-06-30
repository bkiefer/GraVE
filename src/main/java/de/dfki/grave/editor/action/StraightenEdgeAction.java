package de.dfki.grave.editor.action;

import de.dfki.grave.editor.panels.ProjectEditor;
import de.dfki.grave.model.flow.AbstractEdge;

/**
 *
 * @author Patrick Gebhard
 */
public class StraightenEdgeAction extends ReshapeEdgeAction{

  public StraightenEdgeAction(ProjectEditor editor, AbstractEdge edge) {
    super(editor, edge);
  }

  @Override
  protected void reshape() {
    mEdge.straightenEdge();
  }
  
  @Override
  protected String msg() { return "Straighten Edge" ; }
}
