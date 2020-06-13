package de.dfki.grave.editor.action;

//~--- JDK imports ------------------------------------------------------------
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.undo.*;

import de.dfki.grave.editor.panels.WorkSpace;

/**
 * @author Gregor Mehlmann
 */
public abstract class EditorAction implements ActionListener, UndoableEdit {

  protected WorkSpace mWorkSpace;

  protected void refresh() {
    //mWorkSpace.revalidate();
    //mWorkSpace.repaint(100);
    mWorkSpace.refresh(); // TODO: WHY DOES THIS WORK, BUT NOT THE ABOVE?
  }

  public void run() {
    doIt();
    mWorkSpace.getEditor().getUndoManager().addEdit(this);
    refresh();
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
