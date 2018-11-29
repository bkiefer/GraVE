package de.dfki.vsm.editor.action;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import de.dfki.vsm.editor.Node;
import de.dfki.vsm.editor.project.WorkSpacePanel;

/**
 * @author Patrick Gebhard
 */
public class ChangeNodeTypeAction extends EditorAction {

  private WorkSpacePanel mWorkSpace;
  private Node mGUINode = null;

  public ChangeNodeTypeAction(WorkSpacePanel workSpace, Node node) {
    mWorkSpace = workSpace;
    mGUINode = node;
  }

  public void run() {
    mWorkSpace.changeType(mGUINode);
  }

  private class Edit extends AbstractUndoableEdit {

    @Override
    public void undo() throws CannotUndoException {
      run();
    }

    @Override
    public void redo() throws CannotRedoException {
      run();
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
      return "Undo Copying Of Nodes ";
    }

    @Override
    public String getRedoPresentationName() {
      return "Redo Copying Of Nodes ";
    }
  }
}
