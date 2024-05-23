package de.dfki.grave.editor.action;

import de.dfki.grave.editor.panels.ProjectEditor;
import de.dfki.grave.model.flow.CommentBadge;

/**
 * @author Gregor Mehlmann
 * @author Patrick Gebhard
 */
public class CreateCommentAction extends EditorAction {

  private CommentBadge mComment;

  public CreateCommentAction(ProjectEditor editor, CommentBadge comm) {
    super(editor);
    mComment = comm;
  }

  protected void doIt() {
    // add to model
    mComment.getParentNode().addComment(mComment);
    if (onActiveWorkSpace())
      getWorkSpace().addComment(mComment);
  }

  protected void undoIt() {
    mSuperNode.removeComment(mComment);
    if (onActiveWorkSpace())
      getWorkSpace().removeComment(mComment);
  }

  protected String msg() { return "Creation Of Comment"; }
}
