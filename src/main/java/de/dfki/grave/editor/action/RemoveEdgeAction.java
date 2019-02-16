package de.dfki.grave.editor.action;

import de.dfki.grave.editor.panels.WorkSpace;
import de.dfki.grave.model.flow.AbstractEdge;

/**
 * @author Gregor Mehlmann
 */
public class RemoveEdgeAction extends EditorAction {
  private AbstractEdge mEdge = null;

  public RemoveEdgeAction(WorkSpace workSpace, AbstractEdge edge) {
    mWorkSpace = workSpace;
    mEdge = edge;
  }

  public void doIt() {
    mWorkSpace.removeEdge(mEdge);
  }

  public void undoIt() {
    mWorkSpace.addEdge(mEdge);
  }

  @Override
  public String msg() { return "Deletion Of Edges"; }

}
