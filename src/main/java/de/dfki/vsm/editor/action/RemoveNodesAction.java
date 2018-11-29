package de.dfki.vsm.editor.action;

//~--- JDK imports ------------------------------------------------------------
import java.util.HashSet;
import java.util.Set;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

//~--- non-JDK imports --------------------------------------------------------
import de.dfki.vsm.editor.EditorInstance;
import de.dfki.vsm.editor.Node;
import de.dfki.vsm.editor.project.WorkSpacePanel;

/**
 * @author Patrick Gebhard
 */
public class RemoveNodesAction extends EditorAction {

  private Set<Node> mNodes = null;
  private WorkSpacePanel mWorkSpace = null;

  public RemoveNodesAction(WorkSpacePanel workSpace, Node node) {
    mWorkSpace = workSpace;
    mNodes = new HashSet<Node>();
    mNodes.add(node);
  }

  public RemoveNodesAction(WorkSpacePanel workSpace, Set<Node> nodes) {
    mWorkSpace = workSpace;
    mNodes = nodes;
  }

  protected void deleteNodes() {
    mWorkSpace.removeNodes(false, mNodes);
  }

  protected void createNodes() {
  }

  public void run() {
    deleteNodes();
    UndoAction.getInstance().refreshUndoState();
    RedoAction.getInstance().refreshRedoState();
    EditorInstance.getInstance().refresh();
  }

  private class Edit extends AbstractUndoableEdit {

    @Override
    public void undo() throws CannotUndoException {
      createNodes();
    }

    @Override
    public void redo() throws CannotRedoException {
      deleteNodes();
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
      return "Undo Deletion Of Nodes ";
    }

    @Override
    public String getRedoPresentationName() {
      return "Redo Deletion Of Nodes ";
    }
  }
}
