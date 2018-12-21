package de.dfki.vsm.editor.action;

import java.util.ArrayList;
import java.util.Collection;

import de.dfki.vsm.editor.Edge;
import de.dfki.vsm.editor.project.WorkSpace;

/**
 * @author Gregor Mehlmann
 */
public class RemoveEdgesAction extends EditorAction {
  private Collection<Edge> mGUIEdge = null;

  public RemoveEdgesAction(WorkSpace workSpace, Edge edge) {
    mWorkSpace = workSpace;
    mGUIEdge = new ArrayList<>(1);
    mGUIEdge.add(edge);
  }

  public RemoveEdgesAction(WorkSpace workSpace, Collection<Edge> edges) {
    mWorkSpace = workSpace;
    mGUIEdge = edges;
  }

  public void doIt() {
    mWorkSpace.removeEdges(mGUIEdge);
  }

  public void undoIt() {
    mWorkSpace.addEdges(mGUIEdge);
  }

  @Override
  public String msg() { return "Deletion Of Edges"; }

}
