package de.dfki.vsm.editor.action;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

//~--- non-JDK imports --------------------------------------------------------
import de.dfki.vsm.editor.Comment;
import de.dfki.vsm.editor.project.SceneFlowEditor;
import de.dfki.vsm.editor.project.WorkSpacePanel;
import de.dfki.vsm.model.flow.CommentBadge;
import de.dfki.vsm.model.flow.SuperNode;

/**
 * @author Patrick Gebhard
 */
public class RemoveCommentAction extends EditorAction {
  private WorkSpacePanel mWorkSpace;
  private SceneFlowEditor mSceneFlowPane;
  private UndoManager mUndoManager;
  private SuperNode mSuperNode;
  private Comment mGUIComment;
  private CommentBadge mDataComment;

  public RemoveCommentAction(WorkSpacePanel workSpace, Comment c) {
    mWorkSpace = workSpace;
    mSceneFlowPane = mWorkSpace.getSceneFlowEditor();
    mSuperNode = mSceneFlowPane.getActiveSuperNode();
    mUndoManager = mSceneFlowPane.getUndoManager();
    mGUIComment = c;
    mDataComment = mGUIComment.getData();
  }

  public void delete() {
    mSuperNode.removeComment(mDataComment);
    mWorkSpace.remove(mGUIComment);
  }

  public void create() {
    mSuperNode.addComment(mDataComment);
    mWorkSpace.add(new Comment(mWorkSpace, mDataComment));
  }

  public void run() {
    delete();
    mWorkSpace.revalidate();
    mWorkSpace.repaint(100);
    mUndoManager.addEdit(new Edit());
    UndoAction.getInstance().refreshUndoState();
    RedoAction.getInstance().refreshRedoState();
  }

  private class Edit extends AbstractUndoableEdit {

    @Override
    public void undo() throws CannotUndoException {
      create();
      mWorkSpace.revalidate();
      mWorkSpace.repaint(100);
    }

    @Override
    public void redo() throws CannotRedoException {
      delete();
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
      return "Undo Deletion of Comment";
    }

    @Override
    public String getRedoPresentationName() {
      return "Redo Deletion of Comment";
    }
  }
}
