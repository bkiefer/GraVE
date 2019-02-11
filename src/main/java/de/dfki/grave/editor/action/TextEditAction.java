package de.dfki.grave.editor.action;

import javax.swing.undo.UndoableEdit;

import de.dfki.grave.editor.panels.WorkSpace;

/**
 * @author Bernd Kiefer
 */
public class TextEditAction extends EditorAction {

  public TextEditAction(WorkSpace workSpace, UndoableEdit e) {
    mWorkSpace = workSpace;
    mEdit = e;
  }

  protected void doIt() { }

  protected void undoIt() { }

  protected String msg() { return mEdit.toString(); }
}
