package de.dfki.grave.editor.action;

import de.dfki.grave.editor.panels.ProjectEditor;
import de.dfki.grave.model.flow.AbstractEdge;

/**
 * @author Gregor Mehlmann
 */
public class RemoveEdgeAction extends EditorAction {
  private AbstractEdge mEdge = null;

  public RemoveEdgeAction(ProjectEditor editor, AbstractEdge edge) {
    super(editor);
    mEdge = edge;
  }

  protected void doIt() {
    mEdge.getSourceNode().removeEdge(mEdge);
    if (onActiveWorkSpace())
      mEditor.getWorkSpace().removeEdge(mEdge);
  }

  protected void undoIt() {
    mEdge.getSourceNode().addEdge(mEdge);
    if (onActiveWorkSpace())
      mEditor.getWorkSpace().addEdge(mEdge);
  }

  @Override
  public String msg() { return "Deletion Of Edges"; }

}
