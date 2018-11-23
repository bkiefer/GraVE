package de.dfki.vsm.editor.action;

//~--- JDK imports ------------------------------------------------------------
import java.awt.Point;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

//~--- non-JDK imports --------------------------------------------------------
import de.dfki.vsm.editor.Comment;
import de.dfki.vsm.editor.project.WorkSpacePanel;
import de.dfki.vsm.model.flow.SuperNode;
import de.dfki.vsm.model.flow.geom.Boundary;

/**
 * @author Gregor Mehlmann
 * @author Patrick Gebhard
 */
public class CreateCommentAction extends EditorAction {

  private UndoManager mUndoManager = null;
  private WorkSpacePanel mWorkSpace = null;
  private Point mCoordinate = null;
  private Comment mGUIComment;
  private de.dfki.vsm.model.flow.CommentBadge mComment;
  private de.dfki.vsm.model.flow.SuperNode mParentDataNode;
  private SuperNode mSuperNode;

  public CreateCommentAction(WorkSpacePanel workSpace, Point coordinate) {
    mWorkSpace = workSpace;
    mCoordinate = coordinate;
    mUndoManager = mWorkSpace.getSceneFlowEditor().getUndoManager();
    mComment = new de.dfki.vsm.model.flow.CommentBadge();
    mComment.setBoundary(new Boundary(coordinate.x, coordinate.y, 100, 100));
    mParentDataNode = mWorkSpace.getSceneFlowEditor().getActiveSuperNode();
    mSuperNode = mParentDataNode;

    //
    mGUIComment = new de.dfki.vsm.editor.Comment(mWorkSpace, mComment);
  }

  public void delete() {
    mSuperNode.removeComment(mComment);
    mWorkSpace.remove(mGUIComment);
  }

  public void create() {
    mComment.setParentNode(mParentDataNode);
    mParentDataNode.addComment(mComment);
    mWorkSpace.add(mGUIComment);
  }

  public void run() {
    create();
    mUndoManager.addEdit(new Edit());
    UndoAction.getInstance().refreshUndoState();
    RedoAction.getInstance().refreshRedoState();
    mWorkSpace.revalidate();
    mWorkSpace.repaint(100);
  }

  private class Edit extends AbstractUndoableEdit {

    @Override
    public void undo() throws CannotUndoException {
      delete();
      mWorkSpace.revalidate();
      mWorkSpace.repaint(100);
    }

    @Override
    public void redo() throws CannotRedoException {
      create();
      mWorkSpace.revalidate();
      mWorkSpace.repaint(100);
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
      return "Undo Creation Of Comment";
    }

    @Override
    public String getRedoPresentationName() {
      return "Redo Creation Of Comment ";
    }
  }
}
