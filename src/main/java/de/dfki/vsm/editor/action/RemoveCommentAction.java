package de.dfki.vsm.editor.action;

//~--- non-JDK imports --------------------------------------------------------
import de.dfki.vsm.editor.Comment;
import de.dfki.vsm.editor.project.WorkSpace;

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
