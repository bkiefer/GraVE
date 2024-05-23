package de.dfki.grave.editor.action;

import de.dfki.grave.editor.panels.ProjectEditor;
import de.dfki.grave.model.flow.SuperNode;

/**
 * @author Patrick Gebhard
 */
public class ActiveSuperNodeAction extends EditorAction {

  private SuperNode oldSuperNode;
  private SuperNode newSuperNode;

  /**
   * @param editor
   * @param node
   * @param add if true, this goes down (away from the root) in the super node
   *  hierarchy, up otherwise
   */
  public ActiveSuperNodeAction(ProjectEditor editor, SuperNode node) {
    super(editor);
    oldSuperNode = editor.getActiveSuperNode();
    newSuperNode = node;
  }

  @Override
  protected void doIt() {
    mEditor.setActiveSuperNode(newSuperNode);
  }

  @Override
  public void undoIt() {
    mEditor.setActiveSuperNode(oldSuperNode);
  }

  @Override
  public String msg() {
    return "Switch to new super node";
  }
}
