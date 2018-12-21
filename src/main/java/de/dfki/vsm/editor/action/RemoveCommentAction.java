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
    mWorkSpace.removeCommentNew(mGUIComment);
  }

  public void undoIt() {
    mWorkSpace.addCommentNew(mGUIComment);
  }

  public String msg() { return "Deletion of Comment"; }
}
