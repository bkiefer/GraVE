
/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
 */
package de.dfki.grave.editor.action;

import de.dfki.grave.editor.panels.ProjectEditor;
import de.dfki.grave.model.AbstractEdge;
import de.dfki.grave.model.Position;

/**
 * @author Bernd Kiefer
 *
 * For all actions that modify the shape of an edge only with some function,
 * not source or target node
 */
public abstract class ReshapeEdgeAction extends ModifyEdgeAction {

  private boolean done = false;

  public ReshapeEdgeAction(ProjectEditor editor, AbstractEdge edge) {
    super(editor, edge);
  }

  protected abstract void reshape();

  @Override
  protected void doIt() {
    if (! done) {
      reshape();
      mNewDocks = new int[] { mEdge.getSourceDock(), mEdge.getTargetDock() };
      mNewCtrls = new Position[] { mEdge.getSourceCtrlPoint(),
          mEdge.getTargetCtrlPoint() };
    }
    super.doIt();
  }
}
