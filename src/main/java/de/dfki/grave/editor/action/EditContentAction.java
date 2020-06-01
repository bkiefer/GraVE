package de.dfki.grave.editor.action;

import de.dfki.grave.editor.ObserverDocument;
import de.dfki.grave.editor.panels.WorkSpace;

/**
 * @author kiefer
 */
public class EditContentAction extends EditorAction {

  private final ObserverDocument mDocument;
  
  String old, newContent;

  public EditContentAction(WorkSpace workSpace, ObserverDocument c) {
    mWorkSpace = workSpace;
    mDocument = c;
    old = c.getInitialContent();
    newContent = c.getCurrentContent();
  }
  
  @Override
  public void doIt() {
    mDocument.updateModel(newContent);
  }

  @Override
  protected void undoIt() {
    mDocument.updateModel(old);
  }

  @Override
  protected String msg() {
    return "Edit Content";
  }
}
