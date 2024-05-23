package de.dfki.grave.editor.action;

import de.dfki.grave.editor.panels.ProjectEditor;
import de.dfki.grave.model.BasicNode;

/**
 * @author Patrick Gebhard
 */
public class ChangeNodeTypeAction extends EditorAction {

  private BasicNode mPrevNode = null;
  private BasicNode mNode = null;

  public ChangeNodeTypeAction(ProjectEditor editor, BasicNode node) {
    super(editor);
    mNode = node;
  }
    
  protected void doIt() {
    try {
      mPrevNode = mNode.changeType(mPrevNode);
      if (onActiveWorkSpace()) {
        getWorkSpace().changeType(mNode, mPrevNode);
      }
    } catch (Exception e) {
      // complain: operation not legal
      mEditor.setMessageLabelText(e.getMessage());
    }
  }

  protected void undoIt() {
    try {
      mPrevNode.changeType(mNode);
      if (onActiveWorkSpace()) {
        getWorkSpace().changeType(mPrevNode, mNode);
      }
    } catch (Exception e) {
      // complain: operation not legal
      mEditor.setMessageLabelText(e.getMessage());
    }
  }

  protected String msg() { return "Change Node Type"; }
}
