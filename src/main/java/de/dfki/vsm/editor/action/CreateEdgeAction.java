package de.dfki.vsm.editor.action;

import de.dfki.vsm.editor.Edge;
import de.dfki.vsm.editor.Node;
import de.dfki.vsm.editor.project.WorkSpace;
import de.dfki.vsm.model.flow.AbstractEdge;

/**
 * @author Gregor Mehlmann
 */
public class CreateEdgeAction extends EditorAction {

  private Edge mGUIEdge;
  private Node mSourceGUINode, mTargetGUINode;
  private AbstractEdge mEdge;

  public CreateEdgeAction(WorkSpace workSpace, Node sourceNode, Node targetNode,
          AbstractEdge dataEdge) {
    mWorkSpace = workSpace;
    mGUIEdge = null;
    mSourceGUINode = sourceNode;
    mTargetGUINode = targetNode;
    mEdge = dataEdge;
  }


  public void doIt() {
    if (mGUIEdge == null)
      mGUIEdge = mWorkSpace.createEdge(mEdge, mSourceGUINode, mTargetGUINode);
    mWorkSpace.addEdge(mGUIEdge);
  }

  public void undoIt() {
    mWorkSpace.removeEdge(mGUIEdge);
  }

  public String msg() { return "creation of edge"; }

}
