
/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
 */
package de.dfki.grave.editor.action;

import de.dfki.grave.editor.panels.ProjectEditor;
import de.dfki.grave.model.flow.AbstractEdge;

/**
 * @author Bernd Kiefer
 *
 * For all actions that modify the shape of an edge only with some function,
 * not source or target node
 */
public class ShortestEdgeAction extends ReshapeEdgeAction {

  public ShortestEdgeAction(ProjectEditor editor, AbstractEdge edge) {
    super(editor, edge);
  }

  @Override
  protected void reshape() {
    mEdge.straightenEdge();
    if (onActiveWorkSpace())
      getWorkSpace().updateView(mEdge); 
  }


  @Override
  protected String msg() { return "Get Shortest Path"; }
}
