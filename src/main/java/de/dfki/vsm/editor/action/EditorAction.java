package de.dfki.vsm.editor.action;

//~--- JDK imports ------------------------------------------------------------
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import de.dfki.vsm.editor.project.WorkSpace;

/**
 * @author Gregor Mehlmann
 */
public abstract class EditorAction {

  protected WorkSpace mWorkSpace;

  protected void refresh() {
    mWorkSpace.revalidate();
    mWorkSpace.repaint(100);
  }

  public void run() {
    doIt();
    mWorkSpace.getSceneFlowEditor().getUndoManager().addEdit(new Edit());
    UndoAction.getInstance().refreshUndoState();
    RedoAction.getInstance().refreshRedoState();
    refresh();
  }

  protected abstract void doIt();
  protected abstract void undoIt();
  protected abstract String msg();

  public ActionListener getActionListener() {
    return new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        run();
      }
    };
  }

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
    public boolean canUndo() {
      return true;
    }

    @Override
    public boolean canRedo() {
      return true;
    }

    @Override
    public String getUndoPresentationName() {
      return "Undo " + msg();
    }

    @Override
    public String getRedoPresentationName() {
      return "Redo " + msg();
    }
  }

}
