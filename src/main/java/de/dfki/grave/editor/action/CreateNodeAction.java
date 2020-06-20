package de.dfki.grave.editor.action;

import java.util.ArrayList;
import java.util.List;

import de.dfki.grave.editor.panels.ProjectEditor;
import de.dfki.grave.model.flow.BasicNode;

/**
 * @author Gregor Mehlmann
 * @author Patrick Gebhard
 */
public class CreateNodeAction extends EditorAction {

  private List<BasicNode> mNode = new ArrayList<BasicNode>();

  public CreateNodeAction(ProjectEditor editor, BasicNode node) {
    super(editor);
    mNode.add(node);
  }

  protected void doIt() {
    if (onActiveWorkSpace()) 
      getWorkSpace().addNodeView(mNode.get(0));
  }

  protected void undoIt() {
    Object[] edgeLists = mSuperNode.removeNodes(mNode);
    if (onActiveWorkSpace()) 
      getWorkSpace().removeNodes(mNode, edgeLists);
  }

  protected String msg() { return "Creation Of Node"; }
}
