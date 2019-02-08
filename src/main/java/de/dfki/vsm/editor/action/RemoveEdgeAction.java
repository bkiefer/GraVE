package de.dfki.vsm.editor.action;

import de.dfki.vsm.editor.Edge;
import de.dfki.vsm.editor.project.WorkSpace;

/**
 * @author Gregor Mehlmann
 */
public class RemoveEdgeAction extends EditorAction {
  private Edge mGUIEdge = null;

  public RemoveEdgeAction(WorkSpace workSpace, Edge edge) {
    mWorkSpace = workSpace;
    mGUIEdge = edge;
  }

  public void doIt() {
    mWorkSpace.removeEdge(mGUIEdge);
  }

  public void undoIt() {
    mWorkSpace.addEdge(mGUIEdge);
  }

  @Override
  public String msg() { return "Deletion Of Edges"; }

}
