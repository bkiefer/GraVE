package de.dfki.grave.editor.action;

import de.dfki.grave.editor.panels.ProjectEditor;
import de.dfki.grave.model.BasicNode;

/**
 * @author Patrick Gebhard
 */
public class ChangeNodeNameAction extends EditorAction {

  private BasicNode mNode = null;
  private String mNewName, mOldName;

  public ChangeNodeNameAction(ProjectEditor workSpace, BasicNode node, String name){
    super(workSpace);
    mNode = node;
    mOldName = node.getName();
    mNewName = name;
  }

  protected void doIt() {
    mNode.setName(mNewName);
  }

  protected void undoIt() {
    mNode.setName(mOldName);
  }

  protected String msg() { return "Change Node Name"; }
}
