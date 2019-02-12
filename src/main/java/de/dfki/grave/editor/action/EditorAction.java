package de.dfki.grave.editor.action;

//~--- JDK imports ------------------------------------------------------------
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

import de.dfki.grave.editor.panels.UndoRedoProvider;
import de.dfki.grave.editor.panels.WorkSpace;

/**
 * @author Gregor Mehlmann
 */
public abstract class EditorAction implements ActionListener {

  protected WorkSpace mWorkSpace;
  protected UndoableEdit mEdit = null;

  protected void refresh() {
    //mWorkSpace.revalidate();
    //mWorkSpace.repaint(100);
    mWorkSpace.refresh(); // TODO: WHY DOES THIS WORK, BUT NOT THE ABOVE?
  }

  public void run() {
    doIt();
    if (mEdit == null) mEdit = new Edit();
    mWorkSpace.getSceneFlowEditor().getUndoManager().addEdit(mEdit);
    UndoRedoProvider.refreshState();
    refresh();
  }

  protected abstract void doIt();
  protected abstract void undoIt();
  protected abstract String msg();

  public void actionPerformed(ActionEvent event) { run(); }

  @SuppressWarnings("serial")
  private class Edit extends AbstractUndoableEdit {

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
  }

}
