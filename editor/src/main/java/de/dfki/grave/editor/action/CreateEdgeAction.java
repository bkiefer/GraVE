package de.dfki.grave.editor.action;

import de.dfki.grave.editor.panels.ProjectEditor;
import de.dfki.grave.model.AbstractEdge;
import de.dfki.grave.model.BasicNode;

/**
 * @author Gregor Mehlmann
 */
public class CreateEdgeAction extends EditorAction {

  private AbstractEdge mEdge;
  private BasicNode mSourceNode, mTargetNode;

  public CreateEdgeAction(ProjectEditor editor,
      BasicNode sourceNode, BasicNode targetNode, AbstractEdge dataEdge) {
    super(editor);
    mSourceNode = sourceNode;
    mTargetNode = targetNode;
    mEdge = dataEdge;
  }

  protected void doIt() {
    // add to model
    mEdge.connect(mSourceNode, mTargetNode);
    // also computes dock points
    mEdge.straightenEdge();
    mSourceNode.addEdge(mEdge);
    if (onActiveWorkSpace())
      getWorkSpace().addEdge(mEdge);
  }

  protected void undoIt() {
    mEdge.getSourceNode().removeEdge(mEdge);
    if (onActiveWorkSpace())
      getWorkSpace().removeEdge(mEdge);
  }

  public String msg() { return "creation of edge"; }

}
