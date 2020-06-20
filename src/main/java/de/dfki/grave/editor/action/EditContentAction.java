package de.dfki.grave.editor.action;

import de.dfki.grave.editor.ObserverDocument;
import de.dfki.grave.editor.panels.ProjectEditor;

/**
 * @author kiefer
 */
public class EditContentAction extends EditorAction {

  private final ObserverDocument mDocument;
  
  String old, newContent;

  public EditContentAction(ProjectEditor editor, ObserverDocument c) {
    super(editor);
    mDocument = c;
    old = c.getInitialContent();
    newContent = c.getCurrentContent();
  }
  
  @Override
  protected void doIt() {
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
