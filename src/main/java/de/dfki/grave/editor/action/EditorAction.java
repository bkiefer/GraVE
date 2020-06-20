package de.dfki.grave.editor.action;

//~--- JDK imports ------------------------------------------------------------
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

import de.dfki.grave.editor.panels.ProjectEditor;
import de.dfki.grave.editor.panels.WorkSpace;
import de.dfki.grave.model.flow.SuperNode;

/**
 * @author Gregor Mehlmann
 */
public abstract class EditorAction implements ActionListener, UndoableEdit {

  //protected WorkSpace mWorkSpace;
  
  protected ProjectEditor mEditor;
  protected SuperNode mSuperNode;

  public EditorAction(ProjectEditor editor) {
    mEditor = editor;
    mSuperNode = mEditor.getActiveSuperNode();
  }

  protected void refresh() {
    //mWorkSpace.revalidate(); mWorkSpace.repaint(100);
    // TODO: WHY DOES THIS WORK, BUT NOT THE ABOVE?
    // because it updates all elements of the workspace using the observer 
    // update
    if (onActiveWorkSpace())
      mEditor.getWorkSpace().refresh();
  }

  public void run() {
    doIt();
    mEditor.getUndoManager().addEdit(this);
    refresh();
  }

  protected boolean onActiveWorkSpace() {
    return mEditor.getActiveSuperNode() == mSuperNode;
  }
  
  protected WorkSpace getWorkSpace() {
    return mEditor.getWorkSpace();
  }
  
  protected abstract void doIt();
  protected abstract void undoIt();
  protected abstract String msg();

  public void actionPerformed(ActionEvent event) { run(); }

  @Override
  public void undo() throws CannotUndoException {
    undoIt();
    refresh();
  }

  @Override
  public void redo() throws CannotRedoException {
    doIt();
    refresh();
  }

  @Override
  public boolean canUndo() { return true; }

  @Override
  public boolean canRedo() { return true; }

  @Override
  public String getUndoPresentationName() { return "Undo " + msg(); }

  @Override
  public String getRedoPresentationName() { return "Redo " + msg(); }

  public void die() {}

  public boolean addEdit(UndoableEdit e) { return false; }

  public boolean replaceEdit(UndoableEdit e) { return false; }

  public boolean isSignificant() { return true; }

  public String getPresentationName() { return msg(); }
}
