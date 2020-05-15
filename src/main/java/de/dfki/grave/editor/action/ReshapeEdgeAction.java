
/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
 */
package de.dfki.grave.editor.action;

import de.dfki.grave.editor.panels.WorkSpace;
import de.dfki.grave.model.flow.AbstractEdge;
import de.dfki.grave.model.flow.geom.Position;

/**
 * @author Bernd Kiefer
 *
 * For all actions that modify the shape of an edge only with some function,
 * not source or target node
 */
public abstract class ReshapeEdgeAction extends ModifyEdgeAction {

  private boolean done = false;

  public ReshapeEdgeAction(WorkSpace workSpace, AbstractEdge edge) {
    super(workSpace, edge);
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
