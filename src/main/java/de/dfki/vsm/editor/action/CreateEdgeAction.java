package de.dfki.vsm.editor.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import de.dfki.vsm.editor.Edge;
import de.dfki.vsm.editor.Node;
import de.dfki.vsm.editor.project.WorkSpace;
import de.dfki.vsm.model.flow.AbstractEdge;

/**
 * @author Gregor Mehlmann
 */
public class CreateEdgeAction extends EditorAction {

  private Collection<Edge> mGUIEdge;
  private Node mSourceGUINode, mTargetGUINode;
  private AbstractEdge mEdge;

  public CreateEdgeAction(WorkSpace workSpace, Node sourceNode, Node targetNode,
          AbstractEdge dataEdge) {
    mWorkSpace = workSpace;
    mGUIEdge = new ArrayList<>();
    mSourceGUINode = sourceNode;
    mTargetGUINode = targetNode;
    mEdge = dataEdge;
  }


  public void doIt() {
    if (mGUIEdge.isEmpty())
      mGUIEdge.add(mWorkSpace.createEdge(mEdge, mSourceGUINode, mTargetGUINode));
    mWorkSpace.addEdges(mGUIEdge);
  }

  public void undoIt() {
    mWorkSpace.removeEdges(mGUIEdge);
  }

  public String msg() { return "creation of edge"; }

}
