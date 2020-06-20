package de.dfki.grave.editor.action;

import de.dfki.grave.editor.panels.ProjectEditor;
import de.dfki.grave.model.flow.AbstractEdge;
import de.dfki.grave.model.flow.BasicNode;

/**
 * @author Gregor Mehlmann
 */
public class CreateEdgeAction extends EditorAction {

  private AbstractEdge mEdge, mPrototype;
  private BasicNode mSourceNode, mTargetNode;

  public CreateEdgeAction(ProjectEditor editor, 
      BasicNode sourceNode, BasicNode targetNode, AbstractEdge dataEdge) {
    super(editor);
    mSourceNode = sourceNode;
    mTargetNode = targetNode;
    mPrototype = dataEdge;
    mEdge = null;
  }


  protected void doIt() {
    if (mEdge == null) {
      mEdge = mPrototype;
      mEdge.connect(mSourceNode, mTargetNode);
      // also computes dock points
      mEdge.straightenEdge(
          mEditor.getEditorProject().getEditorConfig().sNODEWIDTH);
      mSourceNode.addEdge(mEdge);
    }
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
