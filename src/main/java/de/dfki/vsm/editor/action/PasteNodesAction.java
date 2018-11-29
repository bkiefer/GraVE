package de.dfki.vsm.editor.action;

import java.awt.Point;

//~--- JDK imports ------------------------------------------------------------
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import de.dfki.vsm.editor.project.WorkSpacePanel;

/**
 * @author Patrick Gebhard
 */
public class PasteNodesAction extends EditorAction {

  WorkSpacePanel mWorkSpace = null;
  Point mMousePosition;

  public PasteNodesAction(WorkSpacePanel workSpace, Point p) {
    mWorkSpace = workSpace;
    mMousePosition = p;
  }

  protected void pasteNodes() {
    mWorkSpace.pasteNodesFromClipboard(mMousePosition);
  }

  protected void deleteNodes() {

  }

  public void run() {
    pasteNodes();
  }

  private class Edit extends AbstractUndoableEdit {

    @Override
    public void undo() throws CannotUndoException {
      deleteNodes();
    }

    @Override
    public void redo() throws CannotRedoException {
      pasteNodes();
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
      return "Undo Pasting Of Nodes ";
    }

    @Override
    public String getRedoPresentationName() {
      return "Redo Pasting Of Nodes ";
    }
  }
}
