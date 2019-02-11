package de.dfki.grave.editor.action;

import java.awt.Rectangle;

import de.dfki.grave.editor.Comment;
import de.dfki.grave.editor.panels.WorkSpace;

/**
 * @author Gregor Mehlmann
 * @author Patrick Gebhard
 */
public class MoveCommentAction extends EditorAction {

  private Comment mComment;
  private Rectangle mOldBounds, mNewBounds;

  public MoveCommentAction(WorkSpace workSpace, Comment c, Rectangle oldBounds){
    mWorkSpace = workSpace;
    mComment = c;
    mOldBounds = oldBounds;
    mNewBounds = c.getBounds();
  }

  protected void undoIt() {
    mComment.moveOrResize(mOldBounds);
  }

  protected void doIt() {
    mComment.moveOrResize(mNewBounds);
  }

  protected String msg() {
    return "Resize or Move Commment";
  }
}
