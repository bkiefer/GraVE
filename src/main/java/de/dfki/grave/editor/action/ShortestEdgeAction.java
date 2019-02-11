
/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
 */
package de.dfki.grave.editor.action;

import de.dfki.grave.editor.panels.WorkSpace;

/**
 *
 * @author Souza Putra
 */
public class ShortestEdgeAction extends EditorAction {

  private de.dfki.grave.editor.Edge mGUIEdge = null;
  private WorkSpace mWorkSpace;

  public ShortestEdgeAction(WorkSpace workSpace, de.dfki.grave.editor.Edge edge) {
    mWorkSpace = workSpace;
    mGUIEdge = edge;
  }

  @Override
  protected void doIt() {
    mGUIEdge.rebuildEdgeNicely();
  }

  @Override
  protected void undoIt() {
    // TODO Auto-generated method stub

  }

  @Override
  protected String msg() { return "Get Shortest Path"; }
}
