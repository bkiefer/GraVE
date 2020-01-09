package de.dfki.grave.editor.action;

import de.dfki.grave.editor.panels.WorkSpace;
import de.dfki.grave.model.flow.AbstractEdge;
import de.dfki.grave.model.flow.BasicNode;

/**
 * @author Gregor Mehlmann
 */
public class CreateEdgeAction extends EditorAction {

  private AbstractEdge mEdge, mPrototype;
  private BasicNode mSourceNode, mTargetNode;

  public CreateEdgeAction(WorkSpace workSpace, BasicNode sourceNode, BasicNode targetNode,
          AbstractEdge dataEdge) {
    mWorkSpace = workSpace;
    mSourceNode = sourceNode;
    mTargetNode = targetNode;
    mPrototype = dataEdge;
    mEdge = null;
  }


  public void doIt() {
    if (mEdge == null)
      mEdge = mWorkSpace.createEdge(mPrototype, mSourceNode, mTargetNode);
    mWorkSpace.addEdge(mEdge);
  }

  public void undoIt() {
    mWorkSpace.removeEdge(mEdge);
  }

  public String msg() { return "creation of edge"; }

}
