package de.dfki.grave.editor.action;

import de.dfki.grave.editor.Comment;
import de.dfki.grave.editor.panels.WorkSpace;

/**
 * @author Patrick Gebhard
 */
public class RemoveCommentAction extends EditorAction {
  private Comment mGUIComment;

  public RemoveCommentAction(WorkSpace ws, Comment c) {
    mWorkSpace = ws;
    mGUIComment = c;
  }

  public void doIt() {
    mWorkSpace.removeComment(mGUIComment);
  }

  public void undoIt() {
    mWorkSpace.addComment(mGUIComment);
  }

  public String msg() { return "Deletion of Comment"; }
}
