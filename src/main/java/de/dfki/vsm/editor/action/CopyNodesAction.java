package de.dfki.vsm.editor.action;

//~--- JDK imports ------------------------------------------------------------
import java.util.HashSet;
import java.util.Set;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

//~--- non-JDK imports --------------------------------------------------------
import de.dfki.vsm.editor.Node;
import de.dfki.vsm.editor.project.SceneFlowEditor;
import de.dfki.vsm.editor.project.WorkSpacePanel;

/**
 * @author Patrick Gebhard
 */
public class CopyNodesAction extends EditorAction {

  private Set<Node> mNodes = null;
  WorkSpacePanel mWorkSpace = null;

  public CopyNodesAction(WorkSpacePanel workSpace, Node node) {
    mWorkSpace = workSpace;
    mNodes = new HashSet<Node>();
    mNodes.add(node);
  }

  public CopyNodesAction(WorkSpacePanel workSpace, Set<Node> nodes) {
    mWorkSpace = workSpace;
    mNodes = nodes;
  }

  public void run() {
    mWorkSpace.copyNodesNew(mNodes);
  }

  private class Edit extends AbstractUndoableEdit {

    @Override
    public void undo() throws CannotUndoException {
      //uncopyNodes();
    }

    @Override
    public void redo() throws CannotRedoException {
      //copyNodes();
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
