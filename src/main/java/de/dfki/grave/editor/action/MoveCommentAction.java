package de.dfki.grave.editor.action;

import de.dfki.grave.editor.panels.ProjectEditor;
import de.dfki.grave.model.flow.Boundary;
import de.dfki.grave.model.flow.CommentBadge;

/**
 * @author Gregor Mehlmann
 * @author Patrick Gebhard
 */
public class MoveCommentAction extends EditorAction {

  private CommentBadge mComment;
  private Boundary mOldBounds, mNewBounds;

  public MoveCommentAction(ProjectEditor editor, CommentBadge c, Boundary oldBounds){
    super(editor);
    mComment = c;
    mOldBounds = oldBounds;
    mNewBounds = c.getBoundary();
  }

  protected void undoIt() {
    mComment.setBoundary(mOldBounds);
  }

  protected void doIt() {
    mComment.setBoundary(mNewBounds);

  }

  protected String msg() {
    return "Resize or Move Commment";
  }
}
