package de.dfki.vsm.editor.action;

//~--- JDK imports ------------------------------------------------------------
import java.awt.Point;

//~--- non-JDK imports --------------------------------------------------------
import de.dfki.vsm.editor.Comment;
import de.dfki.vsm.editor.project.WorkSpace;

/**
 * @author Gregor Mehlmann
 * @author Patrick Gebhard
 */
public class CreateCommentAction extends EditorAction {

  private Comment mGUIComment;
  private Point mCoord;

  public CreateCommentAction(WorkSpace workSpace, Point coordinate) {
    mWorkSpace = workSpace;
    mCoord = coordinate;
  }

  protected void undoIt() {
    mWorkSpace.removeComment(mGUIComment);
  }

  protected void doIt() {
    if (mGUIComment == null) {
      mGUIComment = mWorkSpace.createComment(mCoord);
    }
    mWorkSpace.addComment(mGUIComment);
  }

  protected String msg() { return "Creation Of Comment"; }
}
