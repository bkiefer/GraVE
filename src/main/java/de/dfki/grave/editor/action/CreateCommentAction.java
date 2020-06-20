package de.dfki.grave.editor.action;

import de.dfki.grave.editor.panels.ProjectEditor;
import de.dfki.grave.model.flow.Boundary;
import de.dfki.grave.model.flow.CommentBadge;
import de.dfki.grave.model.flow.Position;

/**
 * @author Gregor Mehlmann
 * @author Patrick Gebhard
 */
public class CreateCommentAction extends EditorAction {

  private CommentBadge mComment;
  private Position mCoord;

  public CreateCommentAction(ProjectEditor editor, Position coordinate) {
    super(editor);
    mCoord = coordinate;
  }

  protected void doIt() {
    if (mComment == null) {
      mComment = CommentBadge.createComment(mSuperNode,
          new Boundary(mCoord.getXPos(), mCoord.getYPos(),
              100, 100));
    }
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
