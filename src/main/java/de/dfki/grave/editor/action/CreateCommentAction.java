package de.dfki.grave.editor.action;

//~--- JDK imports ------------------------------------------------------------
import java.awt.Point;

import de.dfki.grave.editor.Comment;
import de.dfki.grave.editor.panels.WorkSpace;

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
