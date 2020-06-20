package de.dfki.grave.editor.action;

import de.dfki.grave.editor.panels.ProjectEditor;
import de.dfki.grave.model.flow.CommentBadge;

/**
 * @author Patrick Gebhard
 */
public class RemoveCommentAction extends EditorAction {
  private CommentBadge mComment;

  public RemoveCommentAction(ProjectEditor editor, CommentBadge c) {
    super(editor);
    mComment = c;
  }

  protected void doIt() {
    mSuperNode.removeComment(mComment);
    if (onActiveWorkSpace())
      mEditor.getWorkSpace().removeComment(mComment);
  }

  protected void undoIt() {
    mSuperNode.addComment(mComment);
    if (onActiveWorkSpace())
      mEditor.getWorkSpace().addComment(mComment);
  }

  public String msg() { return "Deletion of Comment"; }
}
