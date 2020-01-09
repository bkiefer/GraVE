package de.dfki.grave.editor.action;

import de.dfki.grave.editor.panels.WorkSpace;
import de.dfki.grave.model.flow.BasicNode;

/**
 * @author Patrick Gebhard
 */
public class ChangeNodeNameAction extends EditorAction {

  private BasicNode mNode = null;
  private String mNewName, mOldName;

  public ChangeNodeNameAction(WorkSpace workSpace, BasicNode node, String name){
    mWorkSpace = workSpace;
    mNode = node;
    mOldName = node.getName();
    mNewName = name;
  }

  public void doIt() {
    mWorkSpace.changeName(mNode, mNewName);
  }

  public void undoIt() {
    mWorkSpace.changeName(mNode, mOldName);
  }

  protected String msg() { return "Change Node Name"; }
}
